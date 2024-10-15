package com.android.inter.process.framework

import android.os.IBinder
import com.android.inter.process.framework.metadata.ConnectContext
import com.android.inter.process.framework.metadata.FunctionParameter
import com.android.inter.process.framework.metadata.SuspendContext
import com.android.inter.process.framework.metadata.function
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.intrinsics.suspendCoroutineUninterceptedOrReturn

/**
 * basic interface used to get information about remote process,
 * including version of inter-process-calling tool-kit, package name and etc.
 *
 * for further communication operations, two-way connection binding is also included.
 *
 * @author: liuzhongao
 * @since: 2024/9/16 12:28
 */
internal interface BasicConnection {

    /**
     * get version of [BasicConnection] tool kit.
     *
     * encode method like: (000)(000)(000).
     *
     * example: (1.2.1) -> (001002001), (1.20.1) -> (001020001)
     */
    val version: Long

    /**
     * set handle to remote process.
     */
    fun setConnectContext(connectContext: ConnectContext)

    fun call(request: AndroidJvmMethodRequest): Any?

    suspend fun callSuspend(request: AndroidJvmMethodRequest): Any?
}

internal val BasicConnection.iBinder: IBinder
    get() = when (this) {
        is BasicConnectionCaller -> this.androidFunction.function.asBinder()
        is BasicConnectionReceiver -> this.androidFunction.function.asBinder()
        else -> throw IllegalArgumentException("unknown basic connection type: ${this.javaClass}.")
    }

internal fun BasicConnection(androidFunction: AndroidFunction): BasicConnection {
    return BasicConnectionCaller(androidFunction)
}

internal fun BasicConnection(basicConnection: BasicConnection): BasicConnection {
    return BasicConnectionReceiver(basicConnection)
}

private class BasicConnectionCaller(val androidFunction: AndroidFunction) : BasicConnection {

    override val version: Long
        get() {
            val request = BasicConnectionSelfRequest(TYPE_FETCH_BASIC_CONNECTION_VERSION)
            return this.androidFunction.call(request).getOrThrow() as Long
        }

    override fun setConnectContext(connectContext: ConnectContext) {
        val request = BasicConnectionSelfRequest(requestType = TYPE_SET_CONNECT_CONTEXT)
        request.connectContext = connectContext
        this.androidFunction.call(request)
    }

    override fun call(request: AndroidJvmMethodRequest): Any? {
        return this.androidFunction.call(request).getOrThrow()
    }

    override suspend fun callSuspend(request: AndroidJvmMethodRequest): Any? {
        return suspendCoroutineUninterceptedOrReturn { continuation ->
            val newRequest = request.copy(
                suspendContext = SuspendContext(
                    functionParameter = FunctionParameter(
                        functionType = IContinuation::class.java,
                        androidFunction = Continuation::class.java.receiverAndroidFunction(SafeContinuation(continuation))
                    )
                )
            )
            this@BasicConnectionCaller.androidFunction.call(newRequest).getOrThrow()
        }
    }
}

private class BasicConnectionReceiver(basicConnection: BasicConnection) : BasicConnection by basicConnection {
    val androidFunction = AndroidFunction(
        AndroidFunction { request ->
            val result = kotlin.runCatching {
                when (request) {
                    is AndroidJvmMethodRequest -> if (request.suspendContext != null) {
                        val continuation = object : Continuation<Any?> by request.suspendContext.functionParameter.function<IContinuation<Any?>>() {
                            override val context: CoroutineContext get() = ConnectionCoroutineDispatcherScope.coroutineContext
                        }
                        (this::callSuspend as Function2<AndroidRequest, Continuation<*>, AndroidResponse>)(request, continuation)
                    } else this.call(request)
                    is BasicConnectionSelfRequest -> {
                        when (request.requestType) {
                            TYPE_SET_CONNECT_CONTEXT -> this.setConnectContext(requireNotNull(request.connectContext))
                            TYPE_FETCH_BASIC_CONNECTION_VERSION -> this.version
                            else -> null
                        }
                    }
                    else -> null
                }
            }.onFailure { InterProcessLogger.logError(it) }
            DefaultResponse(result.getOrNull(), result.exceptionOrNull())
        }
    )
}