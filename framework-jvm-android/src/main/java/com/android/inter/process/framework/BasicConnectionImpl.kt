package com.android.inter.process.framework

import com.android.inter.process.framework.address.ParcelableAndroidAddress
import com.android.inter.process.framework.metadata.ConnectContext
import com.android.inter.process.framework.reflect.InvocationParameter
import com.android.inter.process.framework.reflect.InvocationReceiver

internal fun BasicConnection(sourceAddress: ParcelableAndroidAddress): BasicConnection {
    return BasicConnectionImpl(sourceAddress)
}

internal class BasicConnectionImpl(sourceAddress: ParcelableAndroidAddress) : BasicConnection {

    override val version: Long get() = BuildConfig.version

    override val remoteAddress: ParcelableAndroidAddress = sourceAddress

    override fun setConnectContext(connectContext: ConnectContext) {}

    override fun call(request: AndroidJvmMethodRequest): Any? {
        val hostClass = request.declaredClassFullName.stringType2ClassType as Class<Any>
        val invocationReceiver = objectPool.getReceiver(hostClass)

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
        val invocationReceiver = objectPool.getReceiver(hostClass)

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