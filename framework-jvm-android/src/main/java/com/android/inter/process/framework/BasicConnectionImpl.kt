package com.android.inter.process.framework

import com.android.inter.process.framework.metadata.ConnectContext
import com.android.inter.process.framework.reflect.InvocationParameter
import kotlin.coroutines.Continuation

internal object BasicConnectionImpl : BasicConnection {

    override val version: Long get() = BuildConfig.version

    override fun setConnectContext(connectContext: ConnectContext) {}

    override fun call(request: AndroidJvmMethodRequest): Any? {
        val hostClass = request.declaredClassFullName.stringTypeConvert as Class<Any>
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
        return kotlin.coroutines.intrinsics.suspendCoroutineUninterceptedOrReturn { continuation ->
            val hostClass = request.declaredClassFullName.stringTypeConvert as Class<Any>
            val invocationReceiver = objectPool.getReceiver(hostClass)

            val invokeParameter = InvocationParameter(
                declaredClassFullName = request.declaredClassFullName,
                methodName = request.methodName,
                uniqueKey = request.uniqueId,
                methodParameterTypeFullNames = if (request.suspendContext != null) {
                    request.methodParameterTypeFullNames + Continuation::class.java.name
                } else request.methodParameterTypeFullNames,
                methodParameterValues = if (request.suspendContext != null) {
                    request.methodParameterValues + continuation
                } else request.methodParameterValues,
            )
            invocationReceiver.invoke(invokeParameter)
        }
    }
}