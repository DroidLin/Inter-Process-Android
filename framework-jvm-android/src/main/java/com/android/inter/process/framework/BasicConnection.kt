package com.android.inter.process.framework

import android.os.IBinder
import com.android.inter.process.framework.address.ParcelableAndroidAddress
import com.android.inter.process.framework.exceptions.BinderDisconnectedException
import com.android.inter.process.framework.metadata.ConnectContext
import com.android.inter.process.framework.metadata.SuspendContext
import com.android.inter.process.framework.metadata.function
import com.android.inter.process.framework.metadata.newBinderFunctionParameter
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.intrinsics.COROUTINE_SUSPENDED
import kotlin.coroutines.intrinsics.suspendCoroutineUninterceptedOrReturn
import kotlin.coroutines.resumeWithException

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
     * get version of [BasicConnectionStub] tool kit.
     *
     * encode method like: (000)(000)(000).
     *
     * example: (1.2.1) -> (001002001), (1.20.1) -> (001020001)
     */
    val version: Long

    val remoteAddress: ParcelableAndroidAddress

    /**
     * set handle to remote process.
     */
    fun setConnectContext(connectContext: ConnectContext)

    fun call(request: AndroidJvmMethodRequest): Any?

    suspend fun callSuspend(request: AndroidJvmMethodRequest): Any?
}

internal val BasicConnection.iBinder: IBinder
    get() = when (this) {
        is BasicConnectionCaller -> this.function.function.asBinder()
        is BasicConnectionReceiver -> this.function.function.asBinder()
        else -> throw IllegalArgumentException("unknown basic connection type: ${this.javaClass}.")
    }

internal val BasicConnection.isConnected: Boolean
    get() = iBinder.isBinderAlive && iBinder.pingBinder()

internal fun BasicConnectionProxy(androidFunction: AndroidFunction): BasicConnection {
    return BasicConnectionCaller(androidFunction)
}

private class BasicConnectionCaller(val function: AndroidFunction) : BasicConnection {

    override val version: Long
        get() {
            val request = BasicConnectionSelfRequest(TYPE_FETCH_BASIC_CONNECTION_VERSION)
            return this.function.call(request).dataOrThrow as Long
        }

    override val remoteAddress: ParcelableAndroidAddress
        get() {
            val request = BasicConnectionSelfRequest(TYPE_FETCH_REMOTE_CONNECTION_ADDRESS)
            return this.function.call(request).dataOrThrow as ParcelableAndroidAddress
        }

    override fun setConnectContext(connectContext: ConnectContext) {
        val request = BasicConnectionSelfRequest(requestType = TYPE_SET_CONNECT_CONTEXT)
        request.connectContext = connectContext
        this.function.call(request)
    }

    override fun call(request: AndroidJvmMethodRequest): Any? {
        return this.function.call(request).dataOrThrow
    }

    override suspend fun callSuspend(request: AndroidJvmMethodRequest): Any? {
        return suspendCoroutineUninterceptedOrReturn { continuation ->
            val newRequest = request.copy(
                suspendContext = SuspendContext(
                    androidBinderFunctionParameter = newBinderFunctionParameter(
                        clazz = Continuation::class.java,
                        androidFunction = Continuation::class.java.receiverAndroidFunction(SafeContinuation(continuation))
                    )
                )
            )
            var deathListener: AndroidFunction.DeathListener? = null
            var data: Any? = null
            try {
                val isSuspendFunction = request.suspendContext != null
                if (isSuspendFunction) {
                    deathListener = object : AndroidFunction.DeathListener {
                        override fun onDead() {
                            function.removeDeathListener(this)
                            continuation.resumeWithException(BinderDisconnectedException("missing connection to remote"))
                        }
                    }
                    function.addDeathListener(deathListener)
                }

                // call remote
                data = function.call(newRequest).dataOrThrow
            }finally {
                if (deathListener != null && data != COROUTINE_SUSPENDED) {
                    function.removeDeathListener(deathListener)
                }
            }
            data
        }
    }
}

internal fun BasicConnectionStub(basicConnection: BasicConnection): BasicConnection {
    return BasicConnectionReceiver(basicConnection)
}

private class BasicConnectionReceiver(basicConnection: BasicConnection) : BasicConnection by basicConnection {
    val function = AndroidFunctionStub(
        AndroidFunction { request ->
            val result = kotlin.runCatching {
                when (request) {
                    is AndroidJvmMethodRequest -> if (request.suspendContext != null) {
                        val continuation = object : Continuation<Any?> by request.suspendContext.androidBinderFunctionParameter.function() {
                            override val context: CoroutineContext get() = ConnectionCoroutineDispatcherScope.coroutineContext
                        }
                        (this::callSuspend as Function2<AndroidRequest, Continuation<*>, AndroidResponse>)(request, continuation)
                    } else this.call(request)

                    is BasicConnectionSelfRequest -> {
                        when (request.requestType) {
                            TYPE_SET_CONNECT_CONTEXT -> this.setConnectContext(requireNotNull(request.connectContext))
                            TYPE_FETCH_BASIC_CONNECTION_VERSION -> this.version
                            TYPE_FETCH_REMOTE_CONNECTION_ADDRESS -> this.remoteAddress
                            else -> null
                        }
                    }
                    else -> null
                }
            }.onFailure { Logger.logError(it) }
            DefaultResponse(result.getOrNull(), result.exceptionOrNull())
        }
    )
}