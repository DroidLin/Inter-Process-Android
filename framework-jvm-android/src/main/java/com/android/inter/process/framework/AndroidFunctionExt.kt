package com.android.inter.process.framework

import com.android.inter.process.framework.address.AndroidAddress
import com.android.inter.process.framework.address.AndroidAddress.Companion.toParcelableAddress
import com.android.inter.process.framework.reflect.InvocationCallerAndroid
import com.android.inter.process.framework.reflect.InvocationReceiver
import com.android.inter.process.framework.reflect.InvocationReceiverAndroid
import com.android.inter.process.framework.reflect.callerFunction

internal fun <T> Class<T>.callerAndroidFunction(androidFunction: AndroidFunction): T {
    val connectionProxy = BasicConnectionProxy(androidFunction)
    val functionCall = AndroidFunctionCallAdapter(basicConnectionGetter = { connectionProxy })
    return (objectPool.getCaller(this, null, functionCall)
        ?: this.callerFunction(invocationCaller = InvocationCallerAndroid(functionCall, null)))
}

/**
 * generate proxies for the passed in instance, this is only used for remote calling.
 */
internal fun <T, P : T> Class<T>.receiverAndroidFunction(instance: P): AndroidFunction {
    if (!this.isInterface) throw IllegalArgumentException("parameter clazz requires interface.")
    val parcelableAddress = (processAddress as AndroidAddress).toParcelableAddress()
    val invocationReceiver = InvocationReceiverAndroid(instance = instance) as InvocationReceiver<Any>
    return BasicConnectionStub(
        basicConnection = BasicConnection(
            sourceAddress = parcelableAddress,
            receiverFactory = { _, _ -> invocationReceiver }
        )
    ).binderFunction
}

internal val Response.dataOrThrow: Any?
    get() {
        if (this.throwable != null) {
            throw requireNotNull(this.throwable) { "throw requires non-null throwable instance." }
        }
        return this.data
    }