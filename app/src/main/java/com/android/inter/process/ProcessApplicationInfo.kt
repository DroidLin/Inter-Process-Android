package com.android.inter.process

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * @author: liuzhongao
 * @since: 2024/9/17 16:00
 */
class ProcessApplicationInfo(private val context: Context): ApplicationInfo {

    override val packageName: String
        get() = this.context.applicationInfo.packageName

    override val processName: String
        get() = App.getProcessName(context)

    override suspend fun fetchProcessName(): String {
        return withContext(Dispatchers.IO) {
            App.getProcessName(context)
        }
    }
}