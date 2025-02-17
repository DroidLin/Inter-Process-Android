package com.android.inter.process.framework

import com.android.inter.process.framework.annotation.IPCService

@IPCService
internal fun interface BasicFunctionCallback {

    operator fun invoke(response: AndroidResponse)
}