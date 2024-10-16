package com.android.inter.process.framework

/**
 * @author liuzhongao
 * @since 2024/9/18 16:17
 */
internal class AndroidAutoConnection(private val basicConnection: BasicConnection) : Connection {

    override suspend fun call(request: Request): Any? {
        return when (request) {
            is JvmReflectMethodRequest -> {
                val androidRequest = AndroidJvmMethodRequest(
                    declaredClassFullName = request.declaredClassFullName,
                    methodName = request.methodName,
                    methodParameterTypeFullNames = request.methodParameterTypeFullNames,
                    methodParameterValues = request.methodParameterValues,
                    uniqueId = request.uniqueId,
                )
                if (request.continuation != null) {
                    this.basicConnection.callSuspend(androidRequest)
                } else {
                    this.basicConnection.call(androidRequest)
                }
            }

            is JvmMethodRequest -> {
                val androidRequest = AndroidJvmMethodRequest(
                    declaredClassFullName = request.clazz.name,
                    methodName = "",
                    methodParameterTypeFullNames = emptyList(),
                    methodParameterValues = request.functionParameters,
                    uniqueId = request.functionIdentifier,
                )
                if (request.isSuspend) {
                    this.basicConnection.callSuspend(androidRequest)
                } else {
                    this.basicConnection.call(androidRequest)
                }
            }

            else -> null
        }
    }
}