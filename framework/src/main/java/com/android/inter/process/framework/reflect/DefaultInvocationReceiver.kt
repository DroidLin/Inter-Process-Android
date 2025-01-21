package com.android.inter.process.framework.reflect

import com.android.inter.process.framework.stringType2ClassType
import java.lang.reflect.Method
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.Continuation
import kotlin.coroutines.intrinsics.suspendCoroutineUninterceptedOrReturn

/**
 * invoker in remote process, this will only be instantiate for the interface implementations.
 */
internal class DefaultInvocationReceiver<T>(private val instance: T) : InvocationReceiver<T> {

    private val jvmMethodCache by lazy { ConcurrentHashMap<String, Method>() }

    override fun invoke(invocationParameter: InvocationParameter): Any? {
        val jvmFunction = invocationParameter.declaredClassFullName.stringType2ClassType
            .findMethod(
                uniqueId = invocationParameter.methodUniqueId,
                functionName = invocationParameter.methodName,
                clazz = arrayOf(*invocationParameter.methodParameterTypeFullNames.stringType2ClassType.toTypedArray())
            )
        val parameterValues = invocationParameter.methodParameterValues
        return jvmFunction.invoke(this.instance, *parameterValues.toTypedArray())
    }

    override suspend fun invokeSuspend(invocationParameter: InvocationParameter): Any? {
        return suspendCoroutineUninterceptedOrReturn { continuation ->
            val jvmFunction = invocationParameter.declaredClassFullName.stringType2ClassType
                .findMethod(
                    uniqueId = invocationParameter.methodUniqueId,
                    functionName = invocationParameter.methodName,
                    clazz = arrayOf(
                        *invocationParameter.methodParameterTypeFullNames.stringType2ClassType.toTypedArray(),
                        Continuation::class.java
                    )
                )
            val parameterValues = invocationParameter.methodParameterValues
            jvmFunction.invoke(this.instance, *parameterValues.toTypedArray(), continuation)
        }
    }

    private fun Class<*>.findMethod(uniqueId: String, functionName: String, vararg clazz: Class<*>): Method {
        return this@DefaultInvocationReceiver.jvmMethodCache[uniqueId] ?: run {
            val declaredMethod = getDeclaredMethod(functionName, *clazz)
            jvmMethodCache[uniqueId] = declaredMethod
            declaredMethod
        }
    }
}