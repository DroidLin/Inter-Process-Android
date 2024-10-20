package com.android.inter.process.framework

import kotlinx.coroutines.runBlocking

/**
 * @author liuzhongao
 * @since 2024/9/18 16:55
 */
fun interface ConnectionCommander {

    suspend fun call(request: Request): Any?
}

fun ConnectionCommander(function: suspend () -> ConnectionCommander): ConnectionCommander {
    return AutoConnectionCommander(function)
}

fun ConnectionCommander.syncCall(request: Request): Any? {
    return runBlocking { call(request) }
}

private class AutoConnectionCommander(val tryConnect: suspend () -> ConnectionCommander) : ConnectionCommander {

    override suspend fun call(request: Request): Any? {
        return this.tryConnect().call(request)
    }
}