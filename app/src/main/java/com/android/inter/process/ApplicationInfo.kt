package com.android.inter.process

import android.os.ParcelFileDescriptor
import com.android.inter.process.framework.annotation.IPCInterface
import com.android.inter.process.framework.annotation.IPCMethod

/**
 * @author: liuzhongao
 * @since: 2024/9/17 16:00
 */
@IPCInterface
interface ApplicationInfo {

    val packageName: String?

    var ParcelFileDescriptor?.processName: String

    suspend fun callRemote(url: String, parameterList: List<Int?>): Int

    suspend fun fetchProcessName(): String

    suspend fun emptyFunction(@IPCMethod callback: () -> Unit)

    suspend fun String.emptyFunction(@IPCMethod callback: () -> Unit)

    fun emptyCallbackFunction(@IPCMethod callback: () -> Unit)

    fun String.emptyCallbackFunction(@IPCMethod callback: () -> Unit)

    suspend fun writeData(fileDescriptor: ParcelFileDescriptor)

    suspend fun getData(type: String): List<String>?

    fun String.isAwesome(): Boolean

    fun callLocal(url: String, key: Int, value: Long): Boolean

    fun isRight(): Boolean

    fun recordLeft()
}