package com.android.inter.process

import android.os.ParcelFileDescriptor
import com.android.inter.process.framework.annotation.IPCService
import com.android.inter.process.framework.annotation.IPCFunction
import com.android.inter.process.test.metadata.SerializableMetadata

/**
 * @author: liuzhongao
 * @since: 2024/9/17 16:00
 */
@IPCService
interface ApplicationInfo {

    val packageName: String?

    var ParcelFileDescriptor?.processName: String

    var mutableSerializableMetadata: SerializableMetadata

    suspend fun callRemote(url: String, parameterList: List<Int?>): Int

    suspend fun fetchProcessName(): String

    suspend fun emptyFunction(@IPCFunction callback: () -> Unit)

    suspend fun String.emptyFunction(@IPCFunction callback: Callback)

    fun emptyCallbackFunction(@IPCFunction callback: Callback)
    fun emptyCallbackFunction1(@IPCFunction callback: Callback)

    fun String.emptyCallbackFunction(@IPCFunction callback: Callback)

    suspend fun writeData(fileDescriptor: ParcelFileDescriptor)

    suspend fun getData(type: String): List<String>?

    fun String.isAwesome(): Boolean

    fun callLocal(url: String, key: Int, value: Long): Boolean

    fun isRight(): Boolean

    fun recordLeft()
}