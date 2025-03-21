package com.android.inter.process.framework.reflect

import com.android.inter.process.framework.objectPool

/**
 * function call destination, the next step is call real instance the business logic passed in.
 *
 * was divided into two different functions, kotlin suspend function runs differently with the
 * default jvm function.
 */
interface InvocationReceiver<T> {

    fun invoke(invocationParameter: InvocationParameter): Any?

    suspend fun invokeSuspend(invocationParameter: InvocationParameter): Any?
}

fun <T> InvocationReceiver(instance: T): InvocationReceiver<T> {
    return object : InvocationReceiver<T> by DefaultInvocationReceiver(instance) {}
}

fun <T : Any, R : T> Class<T>.InvocationReceiver(instance: T): InvocationReceiver<T> {
    return objectPool.getReceiverBuilder(this)?.invoke(instance)
        ?: object : InvocationReceiver<T> by DefaultInvocationReceiver(instance) {}
}

fun <T : Any> Class<T>.receiverFunction(): InvocationReceiver<T> {
    if (!this.isInterface) throw IllegalArgumentException("parameter clazz requires interface.")
    return objectPool.getReceiver(this)
}

data class InvocationParameter(
    val declaredClassFullName: String,
    val methodName: String,
    val uniqueKey: String,
    val methodParameterTypeFullNames: List<String>,
    val methodParameterValues: List<Any?>,
)

val InvocationParameter.methodUniqueId: String
    get() = "${declaredClassFullName}#${methodName}${methodParameterTypeFullNames.joinToString(",", prefix = "(", postfix = ")")}"
