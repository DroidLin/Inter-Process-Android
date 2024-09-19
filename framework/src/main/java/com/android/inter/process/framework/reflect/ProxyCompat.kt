package com.android.inter.process.framework.reflect

import com.android.inter.process.framework.IFunction
import com.android.inter.process.framework.InterProcessObjectPool
import com.android.inter.process.framework.Request
import com.android.inter.process.framework.metadata.JvmMethodRequest
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import kotlin.coroutines.Continuation

/**
 * @author: liuzhongao
 * @since: 2024/9/16 14:13
 */
typealias InvocationCaller = (JvmMethodRequest) -> Any?
typealias InvocationReceiver<T> = (instance: T, invocationParameter: InvocationParameter) -> Any?

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

fun <T : Any> Class<T>.receiverFunction(invocationReceiver: () -> InvocationReceiver<T>): InvocationReceiver<T> {
    if (!this.isInterface) throw IllegalArgumentException("parameter clazz requires interface.")
    return InterProcessObjectPool.getReceiverCache(this) ?: invocationReceiver()
}

data class InvocationParameter(
    val declaredClassFullName: String,
    val methodName: String,
    val uniqueKey: String,
    val methodParameterTypeFullNames: List<String>,
    val methodParameterValues: List<Any?>,
)

data class SuspendParameter(
    val functionType: Class<out Continuation<*>>,
    val function: IFunction<*, *>
)