package com.android.inter.process.framework

import com.android.inter.process.framework.reflect.InvocationParameter
import com.android.inter.process.framework.reflect.InvocationReceiver
import kotlin.coroutines.Continuation

/**
 * @author: liuzhongao
 * @since: 2024/9/16 18:06
 */
internal class StandardInvocationReceiver<T> : InvocationReceiver<T> {

    override fun invoke(instance: T, invocationParameter: InvocationParameter): Any? {
        val hostClass = invocationParameter.declaredClassFullName.stringTypeConvert
        val functionName = invocationParameter.methodName
        val parameterTypes = invocationParameter.methodParameterTypeFullNames.stringTypeConvert
        val parameterValues = invocationParameter.methodParameterValues

        val suspendParameter = invocationParameter.suspendParameter
        return if (suspendParameter != null) {
            val androidFunction = suspendParameter.function as AndroidFunction
            val continuation = suspendParameter.functionType.callerAndroidFunction(androidFunction)
            hostClass.getDeclaredMethod(functionName, *parameterTypes.toTypedArray(), Continuation::class.java)
                .invoke(instance, *parameterValues.toTypedArray(), continuation)
        } else hostClass.getDeclaredMethod(functionName, *parameterTypes.toTypedArray())
            .invoke(instance, *parameterValues.toTypedArray())
    }
}