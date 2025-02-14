package com.android.inter.process

import android.content.Context
import android.os.ParcelFileDescriptor
import com.android.inter.process.framework.ServiceFactory
import com.android.inter.process.framework.annotation.IPCServiceFactory
import com.android.inter.process.test.metadata.SerializableMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileDescriptor
import java.io.FileInputStream

@IPCServiceFactory(interfaceClazz = ApplicationInfo::class)
class ProcessApplicationInfoFactory : ServiceFactory<ApplicationInfo> {
    override fun serviceCreate(): ApplicationInfo {
        return ProcessApplicationInfo(App.context)
    }
}

/**
 * @author: liuzhongao
 * @since: 2024/9/17 16:00
 */
class ProcessApplicationInfo(private val context: Context): ApplicationInfo {

    override val packageName: String
        get() {
            return this.context.applicationInfo.packageName
        }

    override var ParcelFileDescriptor?.processName: String
        set(value) {}
        get() = App.getProcessName(context)
    override var mutableSerializableMetadata: SerializableMetadata = SerializableMetadata.Default

    override suspend fun callRemote(url: String, parameterList: List<Int?>): Int {
        return 0
    }

    override suspend fun fetchProcessName(): String {
        return App.getProcessName(context)
    }

    override suspend fun emptyFunction(callback: () -> Unit) {
        GlobalScope.launch {
            delay(3000)
            callback()
        }
    }

    override suspend fun String.emptyFunction(callback: Callback) {
        TODO("Not yet implemented")
    }

    override fun emptyCallbackFunction(callback: Callback) {
        callback()
    }

    override fun String.emptyCallbackFunction(callback: Callback) {
        TODO("Not yet implemented")
    }

    override fun emptyCallbackFunction1(callback: Callback) {
    }

    override suspend fun writeData(fileDescriptor: ParcelFileDescriptor) {
        fileDescriptor.use {
            FileInputStream(it.fileDescriptor).bufferedReader().use { inputStream ->
                println(inputStream.readText())
            }
        }
    }

    override suspend fun getData(type: String): List<String> {
        return withContext(Dispatchers.IO) {
            delay(1000L)
            listOf("hello", "world", "!")
        }
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