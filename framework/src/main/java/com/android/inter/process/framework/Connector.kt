package com.android.inter.process.framework

/**
 * @author: liuzhongao
 * @since: 2024/9/16 16:31
 */
fun interface Connector<A : Address, F> {

    suspend fun tryConnect(): F
}