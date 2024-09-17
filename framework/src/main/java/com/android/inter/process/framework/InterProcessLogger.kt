package com.android.inter.process.framework

import java.util.LinkedList
import java.util.logging.Level
import java.util.logging.Logger

/**
 * @author: liuzhongao
 * @since: 2024/9/11 23:01
 */
object InterProcessLogger {

    private const val TAG = "InterProcess"

    private val logger = Logger.getLogger(TAG)
    private val logHandlerList = LinkedList<LogHandler>()

    private var debug: Boolean = true

    @JvmStatic
    fun addLogHandler(logHandler: LogHandler) {
        synchronized(this.logHandlerList) {
            if (!this.logHandlerList.contains(logHandler)) {
                this.logHandlerList += logHandler
            }
        }
    }

    @JvmStatic
    fun removeLogHandler(logHandler: LogHandler) {
        synchronized(this.logHandlerList) {
            if (this.logHandlerList.contains(logHandler)) {
                this.logHandlerList -= logHandler
            }
        }
    }

    @JvmStatic
    fun logDebug(message: String) {
        this.notifyLogHandler { onLogMessage(message) }
        if (!this.debug) return
        this.logger.log(Level.WARNING, message)
    }

    @JvmStatic
    fun logError(throwable: Throwable) {
        this.notifyLogHandler { onLogError(throwable) }
        if (!this.debug) return
        throwable.printStackTrace()
    }

    private fun notifyLogHandler(function: LogHandler.() -> Unit) {
        synchronized(this.logHandlerList) {
            if (!this.logHandlerList.isEmpty()) {
                LinkedList(this.logHandlerList)
            } else emptyList()
        }.forEach(function)
    }

}