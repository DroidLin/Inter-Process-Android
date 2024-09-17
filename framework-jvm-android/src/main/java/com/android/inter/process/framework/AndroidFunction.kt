package com.android.inter.process.framework

import android.os.RemoteException
import com.android.inter.process.framework.exceptions.BinderNotConnectedException
import com.android.inter.process.framework.exceptions.RemoteProcessFailureException
import com.android.inter.process.framework.metadata.FunctionParameters
import com.android.inter.process.framework.metadata.request
import com.android.inter.process.framework.metadata.response

/**
 * delegate of [IFunction]
 *
 * @author: liuzhongao
 * @since: 2024/9/15 10:21
 */
internal fun interface AndroidFunction : IFunction<AndroidRequest, AndroidResponse> {

    override fun call(request: AndroidRequest): AndroidResponse
}

internal val AndroidFunction.isAlive: Boolean
    get() = when (this) {
        is FunctionCaller -> this.binderFunction.asBinder().pingBinder()
        else -> true
    }

/**
 * fast way to get aidl function [IFunction] instance.
 */
internal val AndroidFunction.function: Function
    get() = when (this) {
        is FunctionCaller -> this.binderFunction
        is FunctionReceiver -> this.binderFunction
        else -> throw IllegalArgumentException("unknown AndroidFunction Type: ${this.javaClass}")
    }

internal fun AndroidFunction(function: Function): AndroidFunction {
    return FunctionCaller(function)
}

internal fun AndroidFunction(function: AndroidFunction): AndroidFunction {
    return FunctionReceiver(function)
}

/**
 * implementation of [AndroidFunction] for calling remote method.
 */
private class FunctionCaller(val binderFunction: Function) : AndroidFunction {

    override fun call(request: AndroidRequest): AndroidResponse {
        InterProcessLogger.logDebug(">>>>>>>>>>>>>>>>>>>>>>>>> Start Inter Process Call <<<<<<<<<<<<<<<<<<<<<<<<<<<")
        var response: AndroidResponse? = null
        when {
            !this.binderFunction.asBinder().pingBinder() -> {
                InterProcessLogger.logDebug("remote process not connected yet.")
                response = AndroidResponse(
                    data = null,
                    throwable = BinderNotConnectedException("binder not connected yet.")
                )
            }

            else -> {
                val functionParameters = FunctionParameters.obtain()
                try {
                    functionParameters.request = request
                    InterProcessLogger.logDebug("calling ${request.declaredClassFullName}#${request.methodName}")
                    this.binderFunction.call(functionParameters)
                    response = functionParameters.response
                    if (response == null) {
                        InterProcessLogger.logDebug("fail to get response after method call: ${request.declaredClassFullName}#${request.methodName}")
                        response = AndroidResponse(null, null)
                    }
                } catch (e: RemoteException) {
                    InterProcessLogger.logError(e)
                    response = AndroidResponse(null, RemoteProcessFailureException(e.cause))
                } catch (throwable: Throwable) {
                    InterProcessLogger.logError(throwable)
                    response = AndroidResponse(null, throwable)
                } finally {
                    functionParameters.recycle()
                }
            }
        }
        InterProcessLogger.logDebug(">>>>>>>>>>>>>>>>>>>>>>>> End Inter Process Call <<<<<<<<<<<<<<<<<<<<<<<<<<")
        return requireNotNull(response)
    }
}

private class FunctionReceiver(androidFunction: AndroidFunction) : AndroidFunction by androidFunction {

    val binderFunction = object : Function.Stub() {
        override fun call(functionParameters: FunctionParameters?) {
            try {
                InterProcessLogger.logDebug("========================= Receiver Start Inter Process Call =========================")
                val request = functionParameters?.request ?: return
                InterProcessLogger.logDebug("receive calling ${request.declaredClassFullName}#${request.methodName}")
                val response = this@FunctionReceiver.call(request)
                functionParameters.response = response
            } finally {
                InterProcessLogger.logDebug("========================= Receiver Start Inter Process Call =========================")
            }
        }
    }
}