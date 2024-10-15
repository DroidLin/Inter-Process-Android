package com.android.inter.process.framework.reflect

import com.android.inter.process.framework.InterProcessObjectPool
import com.android.inter.process.framework.metadata.JvmMethodRequest
import com.android.inter.process.framework.objectPool
import java.lang.reflect.Proxy

fun interface InvocationCaller {

    fun invoke(request: JvmMethodRequest): Any?
}


fun interface InvocationReceiver<T> {
    fun invoke(invocationParameter: InvocationParameter): Any?
}

fun <T : Any> Class<T>.callerFunction(
    invocationCaller: InvocationCaller
): T {
    if (!this.isInterface) throw IllegalArgumentException("parameter clazz requires interface.")
    return Proxy.newProxyInstance(
        this.classLoader,
        arrayOf(this),
        InterProcessObjectPool.tryGetInvocationHandler(this) {
            DefaultInvocationHandler(invocationCaller)
        }
    ) as T
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

internal val InvocationParameter.methodUniqueId: String
    get() = "${declaredClassFullName}#${methodName}${
        methodParameterTypeFullNames.joinToString(
            ",",
            prefix = "(",
            postfix = ")"
        )
    }"
