package com.android.inter.process.framework

import com.android.inter.process.framework.metadata.ConnectContext
import com.android.inter.process.framework.reflect.InvocationParameter
import com.android.inter.process.framework.reflect.SuspendParameter
import com.android.inter.process.framework.reflect.receiverFunction
import kotlin.coroutines.Continuation

internal object BasicConnectionImpl : BasicConnection {

    override val version: Long get() = BuildConfig.version

    override fun setConnectContext(connectContext: ConnectContext) {}

    override fun call(request: AndroidJvmMethodRequest): Any? {
        val hostClass = request.declaredClassFullName.stringTypeConvert as Class<Any>
        val instance = objectPool.getInstance(hostClass)
        val invokeParameter = InvocationParameter(
            declaredClassFullName = request.declaredClassFullName,
            methodName = request.methodName,
            uniqueKey = request.uniqueId,
            methodParameterTypeFullNames = request.methodParameterTypeFullNames,
            methodParameterValues = request.methodParameterValues,
            suspendParameter = request.suspendContext?.functionParameter?.let {
                SuspendParameter(
                    functionType = it.functionType as Class<out Continuation<*>>,
                    function = it.androidFunction
                )
            },
        )
        return hostClass.receiverFunction { StandardInvocationReceiver() }.invoke(instance, invokeParameter)
    }

    override suspend fun callSuspend(request: AndroidJvmMethodRequest): Any? {
        return withConnectionScope {
            val hostClass = request.declaredClassFullName.stringTypeConvert as Class<Any>
            val instance = objectPool.getInstance(hostClass)
            val invokeParameter = InvocationParameter(
                declaredClassFullName = request.declaredClassFullName,
                methodName = request.methodName,
                uniqueKey = request.uniqueId,
                methodParameterTypeFullNames = request.methodParameterTypeFullNames,
                methodParameterValues = request.methodParameterValues,
                suspendParameter = request.suspendContext?.functionParameter?.let {
                    SuspendParameter(
                        functionType = it.functionType as Class<out Continuation<*>>,
                        function = it.androidFunction
                    )
                },
            )
            hostClass.receiverFunction { StandardInvocationReceiver() }.invoke(instance, invokeParameter)
        }
    }
}