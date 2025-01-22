package com.android.inter.process.framework.reflect

import com.android.inter.process.framework.reflect.AndroidServiceMethod.AndroidReceiverServiceMethod
import com.android.inter.process.framework.stringType2ClassType
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.Continuation
import kotlin.coroutines.intrinsics.suspendCoroutineUninterceptedOrReturn

fun <T : Any> InvocationReceiverAndroid(instance: T): InvocationReceiver<T> {
    return AndroidInvocationReceiver(instance)
}

private class AndroidInvocationReceiver<T : Any>(val instance: T) : InvocationReceiver<T> {

    /**
     * we provide the service method to improve running performance, and
     * avoid expensive costs in [java.lang.Class.getDeclaredMethod]
     */
    private val serviceMethodCache by lazy { ConcurrentHashMap<String, AndroidReceiverServiceMethod>() }

    override fun invoke(invocationParameter: InvocationParameter): Any? {
        return loadServiceMethod(
            hostClass = invocationParameter.declaredClassFullName.stringType2ClassType,
            uniqueId = invocationParameter.methodUniqueId,
            functionName = invocationParameter.methodName,
            clazz = invocationParameter.methodParameterTypeFullNames.stringType2ClassType.toTypedArray()
        ).invoke(this.instance, *invocationParameter.methodParameterValues.toTypedArray())
    }

    override suspend fun invokeSuspend(invocationParameter: InvocationParameter): Any? {
        return suspendCoroutineUninterceptedOrReturn { continuation ->
            loadServiceMethod(
                hostClass = invocationParameter.declaredClassFullName.stringType2ClassType,
                uniqueId = invocationParameter.methodUniqueId,
                functionName = invocationParameter.methodName,
                clazz = arrayOf(*invocationParameter.methodParameterTypeFullNames.stringType2ClassType.toTypedArray(), Continuation::class.java)
            ).invoke(this.instance, *invocationParameter.methodParameterValues.toTypedArray(), continuation)
        }
    }

    private fun loadServiceMethod(
        hostClass: Class<*>,
        uniqueId: String,
        functionName: String,
        vararg clazz: Class<*>
    ): AndroidReceiverServiceMethod = this.serviceMethodCache.getOrPut(uniqueId) {
        val method = hostClass.getDeclaredMethod(functionName, *clazz)
        AndroidServiceMethod.parseReceiverServiceMethod(method)
    }
}