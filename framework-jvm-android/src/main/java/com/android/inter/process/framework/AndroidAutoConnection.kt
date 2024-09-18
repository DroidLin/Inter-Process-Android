package com.android.inter.process.framework

/**
 * @author liuzhongao
 * @since 2024/9/18 16:17
 */
internal class AndroidAutoConnection(private val basicConnection: BasicConnection) : Connection {

    override suspend fun call(request: Request): Any? {
        if (request !is AndroidJvmMethodRequest) return null
        return if (request.suspendContext != null) {
            this.basicConnection.callSuspend(request)
        } else this.basicConnection.call(request)
    }
}