package com.android.inter.process.framework

import com.android.inter.process.framework.metadata.JvmMethodRequest

/**
 * @author liuzhongao
 * @since 2024/9/18 16:17
 */
internal class AndroidAutoConnection(private val basicConnection: BasicConnection) : Connection {

    override suspend fun call(request: Request): Any? {
        if (request !is JvmMethodRequest) return null
        val androidRequest = AndroidJvmMethodRequest(
            declaredClassFullName = request.declaredClassFullName,
            methodName = request.methodName,
            methodParameterTypeFullNames = request.methodParameterTypeFullNames,
            methodParameterValues = request.methodParameterValues,
            uniqueId = request.uniqueId,
        )
        return if (request.continuation != null) {
            this.basicConnection.callSuspend(androidRequest)
        } else this.basicConnection.call(androidRequest)
    }
}