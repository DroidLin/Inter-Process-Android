package com.android.inter.process.framework

import com.android.inter.process.framework.annotation.IPCService

@IPCService
fun interface BasicFunctionCallback {

    fun callback(data: Any?, throwable: Throwable?)
}