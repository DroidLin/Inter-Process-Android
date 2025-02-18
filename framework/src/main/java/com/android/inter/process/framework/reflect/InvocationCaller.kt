package com.android.inter.process.framework.reflect

import com.android.inter.process.framework.FunctionCallAdapter
import com.android.inter.process.framework.JvmReflectMethodRequest
import com.android.inter.process.framework.Request
import com.android.inter.process.framework.objectPool
import com.android.inter.process.framework.reflectRequest
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import kotlin.coroutines.Continuation

fun interface InvocationCaller {

    fun invoke(method: Method, parameters: Array<Any?>?): Any?

    companion object {
        /**
         * parse jvm method into a structured [Request] object.
         */
        @JvmStatic
        fun parseRequest(hostUniqueKey: String?, method: Method, parameters: Array<Any?>?): JvmReflectMethodRequest {
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
                throw IllegalArgumentException("suspend function with invalid parameters.")
            } else parameters)?.toList() ?: emptyList()

            return reflectRequest(
                declaredClassFullName = method.declaringClass.name,
                hostUniqueKey = hostUniqueKey,
                methodName = method.name,
                methodParameterTypeFullNames = newMethodParameterType,
                methodParameterValues = newMethodParameters,
                uniqueId = "",
                continuation = continuation
            )
        }
    }
}

@JvmOverloads
fun InvocationCaller(functionCallAdapter: FunctionCallAdapter, uniqueKey: String? = null): InvocationCaller {
    return object : InvocationCaller by DefaultInvocationCaller(functionCallAdapter, uniqueKey) {}
}

/**
 * dynamic proxy to delegate interface use.
 */
fun <T> Class<T>.callerFunction(
    invocationCaller: InvocationCaller
): T {
    if (!this.isInterface) throw IllegalArgumentException("parameter clazz requires interface.")
    return Proxy.newProxyInstance(
        this.classLoader,
        arrayOf(this)
    ) { obj, method, args -> invocationCaller.invoke(method, args) } as T
}
