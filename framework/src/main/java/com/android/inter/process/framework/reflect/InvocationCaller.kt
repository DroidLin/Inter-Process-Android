package com.android.inter.process.framework.reflect

import com.android.inter.process.framework.FunctionCallTransformer
import com.android.inter.process.framework.IPCObjectPool
import com.android.inter.process.framework.JvmReflectMethodRequest
import java.lang.reflect.Proxy

fun interface InvocationCaller {

    fun invoke(request: JvmReflectMethodRequest): Any?
}

fun InvocationCaller(functionCallTransformer: FunctionCallTransformer): InvocationCaller {
    return object : InvocationCaller by DefaultInvocationCaller(functionCallTransformer = functionCallTransformer) {}
}

fun <T : Any> Class<T>.callerFunction(
    invocationCaller: InvocationCaller
): T {
    if (!this.isInterface) throw IllegalArgumentException("parameter clazz requires interface.")
    return Proxy.newProxyInstance(
        this.classLoader,
        arrayOf(this),
        IPCObjectPool.tryGetInvocationHandler(this) {
            DefaultInvocationHandler(invocationCaller)
        }
    ) as T
}
