package com.android.inter.process.framework.reflect

import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method

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
        return this.function(p0, method, parameters as? Array<Any?>)
//        val methodParameterTypes = method.parameterTypes
//        val isSuspend = methodParameterTypes.lastOrNull() == Continuation::class.java
//        return if (isSuspend) {
//            val fixParameters = requireNotNull(parameters).copyOf(parameters.size - 1)
//            val continuation = requireNotNull(parameters.lastOrNull() as? Continuation<Any?>)
//            (this::invokeSuspend as Function3<Method, Array<out Any?>, Continuation<Any?>, Any?>)
//                .invoke(method, fixParameters, continuation)
//        } else this.invoke(method, parameters)
    }
}