package com.android.inter.process.framework.reflect

import com.android.inter.process.framework.exceptions.ImplementationNotFoundException
import com.android.inter.process.framework.objectPool
import com.android.inter.process.framework.reflect.AndroidServiceMethod.AndroidReceiverServiceMethod
import com.android.inter.process.framework.stringType2ClassType
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.Continuation
import kotlin.coroutines.intrinsics.suspendCoroutineUninterceptedOrReturn

fun <T> InvocationReceiverAndroid(instance: T): InvocationReceiver<T> {
    return AndroidInvocationReceiver(instance)
}

fun <T> Class<T>.InvocationReceiverAndroid(instance: T): InvocationReceiver<T> {
    return objectPool.getReceiverBuilder(this)?.invoke(instance)
        ?: AndroidInvocationReceiver(instance)
}

private class AndroidInvocationReceiver<T>(val instance: T) : InvocationReceiver<T> {

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
        ).invoke(this.instance as Any, *invocationParameter.methodParameterValues.toTypedArray())
    }

    override suspend fun invokeSuspend(invocationParameter: InvocationParameter): Any? {
        return suspendCoroutineUninterceptedOrReturn { continuation ->
            loadServiceMethod(
                hostClass = invocationParameter.declaredClassFullName.stringType2ClassType,
                uniqueId = invocationParameter.methodUniqueId,
                functionName = invocationParameter.methodName,
                clazz = arrayOf(*invocationParameter.methodParameterTypeFullNames.stringType2ClassType.toTypedArray(), Continuation::class.java)
            ).invoke(this.instance as Any, *invocationParameter.methodParameterValues.toTypedArray(), continuation)
        }
    }

    private fun loadServiceMethod(
        hostClass: Class<*>,
        uniqueId: String,
        functionName: String,
        vararg clazz: Class<*>
    ): AndroidReceiverServiceMethod = this.serviceMethodCache.getOrPut(uniqueId) {
        try {
            val method = hostClass.getDeclaredMethod(functionName, *clazz)
            AndroidServiceMethod.parseReceiverServiceMethod(method)
        } catch (exception: NoSuchMethodException) {
            throw ImplementationNotFoundException(exception)
        }
    }
}