package com.android.inter.process.framework

import com.android.inter.process.framework.metadata.ConnectContext
import com.android.inter.process.framework.metadata.function
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext

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

internal val BasicConnection.androidFunction: AndroidFunction
    get() = when (this) {
        is BasicConnectionCaller -> this.androidFunction
        is BasicConnectionReceiver -> this.androidFunction
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
        get() = TODO("Not yet implemented")

    override fun setConnectContext(connectContext: ConnectContext) {
        this.androidFunction.call(SetConnectContext(connectContext))
    }

    override fun call(request: AndroidJvmMethodRequest): Any? {
        return this.androidFunction.call(request).getOrThrow()
    }

    override suspend fun callSuspend(request: AndroidJvmMethodRequest): Any? {
        return withConnectionScope {
            this@BasicConnectionCaller.androidFunction.call(request).getOrThrow()
        }
    }
}

private class BasicConnectionReceiver(basicConnection: BasicConnection) : BasicConnection by basicConnection {
    val androidFunction = AndroidFunction(
        AndroidFunction { request ->
            val result = kotlin.runCatching {
                when (request) {
                    is AndroidJvmMethodRequest -> if (request.suspendContext != null) {
                        val remoteContinuation = request.suspendContext.functionParameter.function<IContinuation<Any?>>()
                        val continuation = object : Continuation<Any?> {
                            override val context: CoroutineContext get() = ConnectionCoroutineDispatcherScope.coroutineContext
                            override fun resumeWith(result: Result<Any?>) {
                                remoteContinuation.resumeWith(result)
                            }
                        }
                        (this::callSuspend as Function2<AndroidRequest, Continuation<*>, AndroidResponse>)(request, continuation)
                    } else this.call(request)

                    is SetConnectContext -> this.setConnectContext(request.connectContext)
                    else -> null
                }
            }.onFailure { InterProcessLogger.logError(it) }
            DefaultResponse(result.getOrNull(), result.exceptionOrNull())
        }
    )
}