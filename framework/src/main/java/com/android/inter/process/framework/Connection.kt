package com.android.inter.process.framework

/**
 * @author liuzhongao
 * @since 2024/9/18 16:55
 */
interface Connection {

    suspend fun call(request: Request): Any?
}

fun Connection(function: suspend () -> Connection): Connection {
    return AutoConnection(function)
}

private class AutoConnection(val tryConnect: suspend () -> Connection) : Connection {

    override suspend fun call(request: Request): Any? {
        return this.tryConnect().call(request)
    }
}