package com.android.inter.process.framework

import com.android.inter.process.framework.reflect.InvocationParameter
import com.android.inter.process.framework.reflect.InvocationReceiver

/**
 * @author: liuzhongao
 * @since: 2024/9/16 18:06
 */
internal object ReflectInvocationReceiver : InvocationReceiver<Any> {

    override fun invoke(instance: Any, invocationParameter: InvocationParameter): Any? {
        val hostClass = invocationParameter.declaredClassFullName.stringTypeConvert
        val functionName = invocationParameter.methodName
        val parameterTypes = invocationParameter.methodParameterTypeFullNames.stringTypeConvert
        val parameterValues = invocationParameter.methodParameterValues
        return hostClass.getDeclaredMethod(functionName, *parameterTypes.toTypedArray())
            .invoke(instance, *parameterValues.toTypedArray())
    }
}