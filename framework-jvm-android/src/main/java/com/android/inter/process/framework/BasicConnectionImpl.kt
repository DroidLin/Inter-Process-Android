package com.android.inter.process.framework

import com.android.inter.process.framework.address.ParcelableAndroidAddress
import com.android.inter.process.framework.metadata.ConnectContext
import com.android.inter.process.framework.reflect.InvocationParameter
import com.android.inter.process.framework.reflect.InvocationReceiver

internal typealias InvocationReceiverFactory<T> = (Class<T>, String?) -> InvocationReceiver<T>

internal val defaultFactory: InvocationReceiverFactory<Any> = { clazz, uniqueKey ->
    objectPool.getReceiver(clazz, uniqueKey)
}

internal fun BasicConnection(
    sourceAddress: ParcelableAndroidAddress,
    receiverFactory: InvocationReceiverFactory<Any> = defaultFactory
): BasicConnection {
    return BasicConnectionImpl(sourceAddress, receiverFactory)
}

/**
 * real implementation in function call processor, all the inter process call will
 * pass through this class.
 */
internal class BasicConnectionImpl(
    sourceAddress: ParcelableAndroidAddress,
    val invocationReceiverGetter: InvocationReceiverFactory<Any>
) : BasicConnection {

    override val version: Long get() = BuildConfig.version

    override val remoteAddress: ParcelableAndroidAddress = sourceAddress

    override fun setConnectContext(connectContext: ConnectContext) {}

    override fun call(request: AndroidJvmMethodRequest): Any? {
        val hostClass = request.declaredClassFullName.stringType2ClassType as Class<Any>
        val invocationReceiver = invocationReceiverGetter(hostClass, request.hostUniqueKey)

        val invocationParameter = InvocationParameter(
            declaredClassFullName = request.declaredClassFullName,
            methodName = request.methodName,
            uniqueKey = request.uniqueId,
            methodParameterTypeFullNames = request.methodParameterTypeFullNames,
            methodParameterValues = request.methodParameterValues,
        )
        return invocationReceiver.invoke(invocationParameter)
    }

    override suspend fun callSuspend(request: AndroidJvmMethodRequest): Any? {
        val hostClass = request.declaredClassFullName.stringType2ClassType as Class<Any>
        val invocationReceiver = invocationReceiverGetter(hostClass, request.hostUniqueKey)

        val invokeParameter = InvocationParameter(
            declaredClassFullName = request.declaredClassFullName,
            methodName = request.methodName,
            uniqueKey = request.uniqueId,
            methodParameterTypeFullNames = request.methodParameterTypeFullNames,
            methodParameterValues = request.methodParameterValues,
        )
        return invocationReceiver.invokeSuspend(invokeParameter)
    }
}