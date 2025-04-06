package com.android.inter.process.framework

import kotlinx.coroutines.runBlocking

/**
 * transform an unspecified request to a specific ipc-request
 *
 * @author liuzhongao
 * @since 2024/9/18 16:55
 */
fun interface FunctionCallAdapter {

    suspend fun call(request: Request): Any?
}

/**
 * blocking call with non-suspend functions
 */
fun FunctionCallAdapter.syncCall(request: Request): Any? {
    return runBlocking { call(request) }
}
