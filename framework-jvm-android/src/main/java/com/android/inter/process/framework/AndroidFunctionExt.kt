package com.android.inter.process.framework

import com.android.inter.process.framework.reflect.InvocationParameter
import com.android.inter.process.framework.reflect.SuspendParameter
import com.android.inter.process.framework.reflect.callerFunction
import com.android.inter.process.framework.reflect.receiverFunction
import kotlin.coroutines.Continuation

/**
 * @author: liuzhongao
 * @since: 2024/9/16 15:55
 */

internal fun <T : Any> Class<T>.callerAndroidFunction(androidFunction: AndroidFunction): T {
    return this.callerFunction(StandardInvocationCaller(tryConnect = { androidFunction }))
}

internal fun <T : Any> Class<T>.receiverAndroidFunction(instance: T): AndroidFunction {
    if (!this.isInterface) throw IllegalArgumentException("parameter clazz requires interface.")
    return AndroidFunction(
        AndroidFunction { request ->
            val receiver = receiverFunction { StandardInvocationReceiver() }
            val invokeParameter = InvocationParameter(
                declaredClassFullName = request.declaredClassFullName,
                methodName = request.methodName,
                uniqueKey = request.uniqueId,
                methodParameterTypeFullNames = request.methodParameterTypeFullNames,
                methodParameterValues = request.methodParameterValues,
                suspendParameter = request.suspendContext?.functionParameter?.let {
                    SuspendParameter(
                        functionType = it.functionType as Class<out Continuation<*>>,
                        function = it.androidFunction
                    )
                },
            )
            val result = runCatching { receiver.invoke(instance, invokeParameter) }
                .onFailure { InterProcessLogger.logError(it) }
            AndroidResponse(result.getOrNull(), result.exceptionOrNull())
        }
    )
}

internal fun AndroidResponse.getOrThrow(): Any? {
    if (this.throwable != null) {
        throw this.throwable
    }
    return this.data
}