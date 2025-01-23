package com.android.inter.process

import com.android.inter.process.framework.annotation.IPCService

//@IPCService
fun interface Callback {
    operator fun invoke()
}