package com.android.inter.process.framework

import android.os.IBinder
import com.android.inter.process.framework.address.ParcelableAndroidAddress
import com.android.inter.process.framework.exceptions.BinderDisconnectedException
import com.android.inter.process.framework.metadata.ConnectContext
import com.android.inter.process.framework.metadata.SuspendContext
import com.android.inter.process.framework.metadata.function
import com.android.inter.process.framework.metadata.newBinderFunctionMetadata
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.intrinsics.COROUTINE_SUSPENDED
import kotlin.coroutines.intrinsics.suspendCoroutineUninterceptedOrReturn
import kotlin.coroutines.resume
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

internal val BasicConnection.binderFunction: AndroidFunction
    get() = when (this) {
        is BasicConnectionCaller -> this.function
        is BasicConnectionReceiver -> this.function
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
        checkRequest(request)
        return this.function.call(request).dataOrThrow
    }

    override suspend fun callSuspend(request: AndroidJvmMethodRequest): Any? {
        checkRequest(request)
        return suspendCoroutineUninterceptedOrReturn { continuation ->
            val safeContinuation = SafeContinuation(continuation)
            var deathListener: AndroidFunction.DeathListener? = null
            var data: Any? = null
            try {
                // resume suspended function if remote binder is dead.
                deathListener = object : AndroidFunction.DeathListener {
                    override fun onDead() {
                        function.removeDeathListener(this)
                        safeContinuation.resumeWithException(BinderDisconnectedException("missing connection to remote"))
                    }
                }
                function.addDeathListener(deathListener)

                val functionCallback = BasicFunctionCallback { response ->
                    if (response.throwable != null) {
                        safeContinuation.resumeWithException(requireNotNull(response.throwable))
                    } else safeContinuation.resume(response.data)
                }
                val newRequest = request.copy(
                    suspendContext = SuspendContext(
                        androidBinderFunctionMetadata = newBinderFunctionMetadata(
                            clazz = BasicFunctionCallback::class.java,
                            androidFunction = BasicFunctionCallback::class.java.receiverAndroidFunction(functionCallback)
                        )
                    )
                )
                // call remote
                data = function.call(newRequest).dataOrThrow
            } finally {
                // we have to remove death listener if function is not suspended.
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
    val function = AndroidFunctionStub { request ->
        val result = kotlin.runCatching {
            when (request) {
                is AndroidJvmMethodRequest -> if (request.suspendContext != null) {
                    val functionCallback = request.suspendContext.androidBinderFunctionMetadata.function<BasicFunctionCallback>()
                    val continuation = object : Continuation<Any?> {
                        override val context: CoroutineContext get() = ConnectionCoroutineDispatcherScope.coroutineContext
                        override fun resumeWith(result: Result<Any?>) {
                            val response = result.toResponse()
                            val checkResult = kotlin.runCatching { checkResponse(response) }
                            if (checkResult.isFailure) {
                                functionCallback(DefaultResponse(null, checkResult.exceptionOrNull()))
                                return
                            }
                            functionCallback(response)
                        }
                    }
                    (this::callSuspend as Function2<AndroidRequest, Continuation<*>, Any?>)(request, continuation)
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
            }?.also(::checkResponse)
        }.onFailure { Logger.logError(it) }
        result.toResponse()
    }
}

private fun <T> Result<T>.toResponse(): AndroidResponse {
    val data = getOrNull()
    val exception = exceptionOrNull()
    if (data == null && exception == null) {
        return NoResponse()
    }
    return DefaultResponse(getOrNull(), exceptionOrNull())
}