package com.android.inter.process.framework

/**
 * @author liuzhongao
 * @since 2024/9/18 16:17
 */
internal class AndroidFunctionCallTransformer(private val basicConnection: BasicConnection) : FunctionCallTransformer {

    override suspend fun transform(request: Request): Any? {
        return when (request) {
            is JvmReflectMethodRequest -> {
                val jvmRequest = AndroidJvmMethodRequest(
                    declaredClassFullName = request.declaredClassFullName,
                    methodName = request.methodName,
                    methodParameterTypeFullNames = request.methodParameterTypeFullNames,
                    methodParameterValues = request.methodParameterValues,
                    uniqueId = request.uniqueId,
                )
                if (request.continuation != null) {
                    this.basicConnection.callSuspend(jvmRequest)
                } else {
                    this.basicConnection.call(jvmRequest)
                }
            }

            is JvmMethodRequest -> {
                val jvmRequest = AndroidJvmMethodRequest(
                    declaredClassFullName = request.clazz.name,
                    methodName = "",
                    methodParameterTypeFullNames = emptyList(),
                    methodParameterValues = request.functionParameters,
                    uniqueId = request.functionIdentifier,
                )
                if (request.isSuspend) {
                    this.basicConnection.callSuspend(jvmRequest)
                } else {
                    this.basicConnection.call(jvmRequest)
                }
            }

            else -> null
        }
    }
}