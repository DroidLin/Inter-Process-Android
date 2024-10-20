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

    private val jvmMethodCache: MutableMap<String, Method> by lazy { ConcurrentHashMap() }

    override fun invoke(invocationParameter: InvocationParameter): Any? {
        val jvmFunction = this.jvmMethodCache[invocationParameter.methodUniqueId] ?: run {
            val hostClass = invocationParameter.declaredClassFullName.stringType2ClassType
            val functionName = invocationParameter.methodName
            val parameterTypes = invocationParameter.methodParameterTypeFullNames.stringType2ClassType
            val declaredMethod = hostClass.getDeclaredMethod(functionName, *parameterTypes.toTypedArray())
            jvmMethodCache[invocationParameter.methodUniqueId] = declaredMethod
            declaredMethod
        }

        requireNotNull(jvmFunction) { "do not found method satisfied with name: ${invocationParameter.methodName} in class: ${invocationParameter.declaredClassFullName}" }

        val parameterValues = invocationParameter.methodParameterValues
        return jvmFunction.invoke(this.instance, *parameterValues.toTypedArray())
    }

    override suspend fun invokeSuspend(invocationParameter: InvocationParameter): Any? {
        return suspendCoroutineUninterceptedOrReturn { continuation ->
            val jvmFunction = this.jvmMethodCache[invocationParameter.methodUniqueId] ?: run {
                val hostClass = invocationParameter.declaredClassFullName.stringType2ClassType
                val functionName = invocationParameter.methodName
                val parameterTypes = invocationParameter.methodParameterTypeFullNames.stringType2ClassType
                val declaredMethod = hostClass.getDeclaredMethod(functionName, *parameterTypes.toTypedArray(), Continuation::class.java)
                jvmMethodCache[invocationParameter.methodUniqueId] = declaredMethod
                declaredMethod
            }

            requireNotNull(jvmFunction) { "do not found method satisfied with name: ${invocationParameter.methodName} in class: ${invocationParameter.declaredClassFullName}" }

            val parameterValues = invocationParameter.methodParameterValues
            jvmFunction.invoke(this.instance, *parameterValues.toTypedArray(), continuation)
        }
    }
}