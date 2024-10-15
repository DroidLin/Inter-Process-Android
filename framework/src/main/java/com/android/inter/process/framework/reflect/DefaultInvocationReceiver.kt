package com.android.inter.process.framework.reflect

import com.android.inter.process.framework.stringTypeConvert
import java.lang.reflect.Method
import java.util.concurrent.ConcurrentHashMap

/**
 * @author: liuzhongao
 * @since: 2024/9/16 15:09
 */
class DefaultInvocationReceiver<T>(private val instance: T) : InvocationReceiver<T> {

    private val jvmMethodCache: MutableMap<String, Method> = ConcurrentHashMap()

    override fun invoke(invocationParameter: InvocationParameter): Any? {
        val jvmFunction = this.jvmMethodCache[invocationParameter.methodUniqueId] ?: run {
            val hostClass = invocationParameter.declaredClassFullName.stringTypeConvert
            val functionName = invocationParameter.methodName
            val parameterTypes = invocationParameter.methodParameterTypeFullNames.stringTypeConvert
            val declaredMethod = hostClass.getDeclaredMethod(functionName, *parameterTypes.toTypedArray())
            jvmMethodCache[invocationParameter.methodUniqueId] = declaredMethod
            declaredMethod
        }

        requireNotNull(jvmFunction) { "do not found method satisfied with name: ${invocationParameter.methodName} in class: ${invocationParameter.declaredClassFullName}" }

        val parameterValues = invocationParameter.methodParameterValues
        return jvmFunction.invoke(this.instance, *parameterValues.toTypedArray())
    }
}