package com.android.inter.process.framework

/**
 * @author liuzhongao
 * @since 2024/9/18 16:17
 */
internal class AndroidFunctionCallAdapter(private val basicConnectionGetter: suspend () -> BasicConnection) : FunctionCallAdapter {

    override suspend fun call(request: Request): Any? {
        val basicConnection = this.basicConnectionGetter()
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
                    basicConnection.callSuspend(jvmRequest)
                } else {
                    basicConnection.call(jvmRequest)
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
                    basicConnection.callSuspend(jvmRequest)
                } else {
                    basicConnection.call(jvmRequest)
                }
            }

            else -> throw IllegalArgumentException("Unsupported request type: ${request.javaClass.name}.")
        }
    }
}