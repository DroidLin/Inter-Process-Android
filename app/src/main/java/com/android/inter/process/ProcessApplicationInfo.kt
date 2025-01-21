package com.android.inter.process

import android.content.Context
import android.os.ParcelFileDescriptor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileDescriptor
import java.io.FileInputStream

/**
 * @author: liuzhongao
 * @since: 2024/9/17 16:00
 */
class ProcessApplicationInfo(private val context: Context): ApplicationInfo {

    override val packageName: String
        get() {
            return this.context.applicationInfo.packageName
        }

    override var String?.processName: String
        set(value) {}
        get() = App.getProcessName(context)

    override suspend fun callRemote(url: String, parameterList: List<Int?>): Int {
        return 0
    }

    override suspend fun fetchProcessName(): String {
        return App.getProcessName(context)
    }

    override suspend fun writeData(fileDescriptor: ParcelFileDescriptor) {
        FileInputStream(fileDescriptor.fileDescriptor).bufferedReader().use { inputStream ->
            println(inputStream.readText())
        }
    }

    override suspend fun getData(type: String): List<String>? {
        return listOf("hello", "world", "!")
    }

    override fun String.isAwesome(): Boolean {
        return this.length > 10
    }

    override fun callLocal(url: String, key: Int, value: Long): Boolean {
        return url.isEmpty()
    }

    override fun isRight(): Boolean {
        return false
    }

    override fun recordLeft() {
    }
}