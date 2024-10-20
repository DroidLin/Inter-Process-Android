package com.android.inter.process

import com.android.inter.process.framework.annotation.IPCInterface

/**
 * @author: liuzhongao
 * @since: 2024/9/17 16:00
 */
@IPCInterface
interface ApplicationInfo {

    val packageName: String?

    var String?.processName: String

    suspend fun callRemote(url: String, parameterList: List<Int?>): Int

    suspend fun fetchProcessName(): String

    fun String.isAwesome(): Boolean

    fun callLocal(url: String, key: Int, value: Long): Boolean

    fun isRight(): Boolean

    fun recordLeft()
}