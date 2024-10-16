package com.android.inter.process.framework

import com.android.inter.process.framework.metadata.ConnectContext
import com.android.inter.process.framework.reflect.InvocationParameter

internal object BasicConnectionImpl : BasicConnection {

    override val version: Long get() = BuildConfig.version

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