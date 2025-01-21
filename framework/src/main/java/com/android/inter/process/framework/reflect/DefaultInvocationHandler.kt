package com.android.inter.process.framework.reflect

import com.android.inter.process.framework.JvmReflectMethodRequest
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

    override fun invoke(p0: Any?, method: Method?, parameters: Array<Any?>?): Any? {
        require(p0 != null) { "host instance must not be null." }
        require(method != null) { "invalid invocation handler call, p0 = $p0" }

        return this.function.invoke(method, parameters)
    }
}