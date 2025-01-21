package com.android.inter.process.framework.reflect

import com.android.inter.process.framework.FunctionCallTransformer
import com.android.inter.process.framework.IPCObjectPool
import com.android.inter.process.framework.JvmReflectMethodRequest
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import kotlin.coroutines.Continuation

fun interface InvocationCaller {

    fun invoke(method: Method, parameters: Array<Any?>?): Any?

    companion object {
        @JvmStatic
        fun parseRequest(method: Method, parameters: Array<Any?>?): JvmReflectMethodRequest {
            val methodParameterTypes: Array<Class<*>> = method.parameterTypes
            val isSuspend = methodParameterTypes.lastOrNull() == Continuation::class.java
            val continuation =
                parameters?.lastOrNull()?.takeIf { it is Continuation<*> } as? Continuation<Any?>

            val newMethodParameterType = (if (isSuspend) {
                methodParameterTypes.copyOf(methodParameterTypes.size - 1)
            } else methodParameterTypes).mapNotNull { it?.name }
            val newMethodParameters = (if (isSuspend && parameters != null) {
                parameters.copyOf(parameters.size - 1)
            } else if (isSuspend) {
                throw IllegalArgumentException("suspend function with null origin parameters in invocation handler.")
            } else parameters)?.toList() ?: emptyList()

            return JvmReflectMethodRequest(
                declaredClassFullName = method.declaringClass.name,
                methodName = method.name,
                methodParameterTypeFullNames = newMethodParameterType,
                methodParameterValues = newMethodParameters,
                uniqueId = "",
                continuation = continuation
            )
        }
    }
}

fun InvocationCaller(functionCallTransformer: FunctionCallTransformer): InvocationCaller {
    return object : InvocationCaller by DefaultInvocationCaller(functionCallTransformer = functionCallTransformer) {}
}

fun <T> Class<T>.callerFunction(
    invocationCaller: InvocationCaller
): T {
    if (!this.isInterface) throw IllegalArgumentException("parameter clazz requires interface.")
    return Proxy.newProxyInstance(
        this.classLoader,
        arrayOf(this),
        IPCObjectPool.tryGetInvocationHandler(this) {
            DefaultInvocationHandler(invocationCaller)
        }
    ) as T
}
