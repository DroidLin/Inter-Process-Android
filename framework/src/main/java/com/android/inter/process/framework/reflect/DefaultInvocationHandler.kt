package com.android.inter.process.framework.reflect

import com.android.inter.process.framework.metadata.JvmMethodRequest
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import kotlin.coroutines.Continuation

/**
 * @author: liuzhongao
 * @since: 2024/9/8 23:30
 */
internal class DefaultInvocationHandler(
    private val function: InvocationCaller,
) : InvocationHandler {

    override fun invoke(p0: Any?, method: Method?, parameters: Array<Any>?): Any? {
        require(p0 != null) { "host instance must not be null." }
        require(method != null) { "invalid invocation handler call, p0 = $p0" }

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

        val request = JvmMethodRequest(
            declaredClassFullName = method.declaringClass.name,
            methodName = method.name,
            methodParameterTypeFullNames = newMethodParameterType,
            methodParameterValues = newMethodParameters,
            uniqueId = "",
            continuation = continuation
        )

        return this.function.invoke(request)
    }
}