package com.android.inter.process.framework

import com.android.inter.process.framework.metadata.function
import com.android.inter.process.framework.reflect.DefaultInvocationReceiver
import com.android.inter.process.framework.reflect.InvocationParameter
import com.android.inter.process.framework.reflect.InvocationReceiver
import com.android.inter.process.framework.reflect.callerFunction
import com.android.inter.process.framework.reflect.receiverFunction
import kotlin.coroutines.Continuation

/**
 * @author: liuzhongao
 * @since: 2024/9/16 15:55
 */

internal fun <T : Any> Class<T>.callerAndroidFunction(androidFunction: AndroidFunction): T {
    return this.callerFunction(StandardInvocationCaller(connection = AndroidAutoConnection(BasicConnection(androidFunction))))
}

internal fun <T : Any> Class<T>.receiverAndroidFunction(instance: T): AndroidFunction {
    if (!this.isInterface) throw IllegalArgumentException("parameter clazz requires interface.")
    return AndroidFunction(
        AndroidFunction { request ->
            if (request !is AndroidJvmMethodRequest) return@AndroidFunction DefaultResponse(null, null)
            val receiver = object : InvocationReceiver<T> by DefaultInvocationReceiver(instance) {}
            val invokeParameter = InvocationParameter(
                declaredClassFullName = request.declaredClassFullName,
                methodName = request.methodName,
                uniqueKey = request.uniqueId,
                methodParameterTypeFullNames = request.methodParameterTypeFullNames,
                methodParameterValues = request.methodParameterValues,
            )
            val result = kotlin.runCatching {
                val continuation = request.suspendContext?.functionParameter?.function<Continuation<Any?>>()
                if (continuation != null) {
                    val suspendFunction = receiver::invokeSuspend as Function2<InvocationParameter, Continuation<Any?>, Any?>
                    suspendFunction(invokeParameter, continuation)
                } else {
                    receiver.invoke(invokeParameter)
                }
            }.onFailure { InterProcessLogger.logError(it) }
            DefaultResponse(result.getOrNull(), result.exceptionOrNull())
        }
    )
}

internal val Response.dataCompat: Any?
    get() {
        if (this.throwable != null) {
            throw requireNotNull(this.throwable)
        }
        return this.data
    }