package com.android.inter.process.framework.reflect

import com.android.inter.process.framework.ConnectionCommander
import com.android.inter.process.framework.InterProcessObjectPool
import com.android.inter.process.framework.JvmReflectMethodRequest
import java.lang.reflect.Proxy

fun interface InvocationCaller {

    fun invoke(request: JvmReflectMethodRequest): Any?
}

fun InvocationCaller(connectionCommander: ConnectionCommander): InvocationCaller {
    return object : InvocationCaller by DefaultInvocationCaller(connectionCommander = connectionCommander) {}
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
