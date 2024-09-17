package com.android.inter.process.framework

/**
 * @author: liuzhongao
 * @since: 2024/9/16 11:28
 */
interface LogHandler {

    fun onLogMessage(message: String)

    fun onLogError(throwable: Throwable)
}