package com.android.inter.process.framework

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

/**
 * transform an unspecified request to a specific ipc-request
 *
 * @author liuzhongao
 * @since 2024/9/18 16:55
 */
fun interface FunctionCallTransformer {

    suspend fun transform(request: Request): Any?
}

fun FunctionCallTransformer(function: suspend () -> FunctionCallTransformer): FunctionCallTransformer {
    return AutoFunctionCallTransformer(function)
}

fun FunctionCallTransformer.syncTransform(request: Request): Any? {
    return runBlocking(Dispatchers.Unconfined) { transform(request) }
}

private class AutoFunctionCallTransformer(val tryConnect: suspend () -> FunctionCallTransformer) : FunctionCallTransformer {

    override suspend fun transform(request: Request): Any? {
        return this.tryConnect().transform(request)
    }
}