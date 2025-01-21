package com.android.inter.process

import android.os.ParcelFileDescriptor
import com.android.inter.process.framework.annotation.IPCInterface
import java.io.FileDescriptor

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

    suspend fun writeData(fileDescriptor: ParcelFileDescriptor)

    suspend fun getData(type: String): List<String>?

    fun String.isAwesome(): Boolean

    fun callLocal(url: String, key: Int, value: Long): Boolean

    fun isRight(): Boolean

    fun recordLeft()
}