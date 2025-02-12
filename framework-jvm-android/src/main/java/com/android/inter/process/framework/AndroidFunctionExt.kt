package com.android.inter.process.framework

import com.android.inter.process.framework.address.AndroidAddress
import com.android.inter.process.framework.address.AndroidAddress.Companion.toParcelableAddress
import com.android.inter.process.framework.metadata.function
import com.android.inter.process.framework.reflect.InvocationCallerAndroid
import com.android.inter.process.framework.reflect.InvocationParameter
import com.android.inter.process.framework.reflect.InvocationReceiver
import com.android.inter.process.framework.reflect.InvocationReceiverAndroid
import com.android.inter.process.framework.reflect.callerFunction
import kotlin.coroutines.Continuation

internal fun <T> Class<T>.callerAndroidFunction(androidFunction: AndroidFunction): T {
    val connectionProxy = BasicConnectionProxy(androidFunction)
    val functionCall = AndroidFunctionCallAdapter(basicConnection = { connectionProxy })
    return (objectPool.getCaller(clazz = this, functionCallAdapter = functionCall)
        ?: this.callerFunction(invocationCaller = InvocationCallerAndroid(functionCallAdapter = functionCall)))
}

/**
 * generate proxies for the passed in instance, this is only used for remote calling.
 */
internal fun <T> Class<T>.receiverAndroidFunction(instance: T): AndroidFunction {
    if (!this.isInterface) throw IllegalArgumentException("parameter clazz requires interface.")
    val parcelableAddress = (IPCManager.currentAddress as AndroidAddress).toParcelableAddress()
    return BasicConnectionStub(
        basicConnection = BasicConnection(
            sourceAddress = parcelableAddress,
            receiverFactory = { InvocationReceiverAndroid(instance = instance) as InvocationReceiver<Any> }
        )
    ).binderFunction
//    return AndroidFunctionStub(
//        AndroidFunction { request ->
//            if (request !is AndroidJvmMethodRequest) return@AndroidFunction DefaultResponse(null, null)
//            val invokeParameter = InvocationParameter(
//                declaredClassFullName = request.declaredClassFullName,
//                methodName = request.methodName,
//                uniqueKey = request.uniqueId,
//                methodParameterTypeFullNames = request.methodParameterTypeFullNames,
//                methodParameterValues = request.methodParameterValues,
//            )
//            val result = kotlin.runCatching {
//                val continuation = request.suspendContext?.androidBinderFunctionMetadata?.function<Continuation<Any?>>()
//                if (continuation != null) {
//                    val suspendFunction = receiver::invokeSuspend as Function2<InvocationParameter, Continuation<Any?>, Any?>
//                    suspendFunction(invokeParameter, continuation)
//                } else {
//                    receiver.invoke(invokeParameter)
//                }
//            }.onFailure { Logger.logError(it) }
//            DefaultResponse(result.getOrNull(), result.exceptionOrNull())
//        }
//    )
}

internal val Response.dataOrThrow: Any?
    get() {
        if (this.throwable != null) {
            throw requireNotNull(this.throwable) { "throw requires non-null throwable instance." }
        }
        return this.data
    }