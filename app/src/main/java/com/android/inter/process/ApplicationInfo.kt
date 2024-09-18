package com.android.inter.process

/**
 * @author: liuzhongao
 * @since: 2024/9/17 16:00
 */
interface ApplicationInfo {

    val packageName: String

    val processName: String

    suspend fun fetchProcessName(): String
}