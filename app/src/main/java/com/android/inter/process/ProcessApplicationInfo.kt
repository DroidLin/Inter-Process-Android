package com.android.inter.process

import android.content.Context

/**
 * @author: liuzhongao
 * @since: 2024/9/17 16:00
 */
class ProcessApplicationInfo(private val context: Context): ApplicationInfo {

    override val packageName: String
        get() = this.context.applicationInfo.packageName

    override val processName: String
        get() = App.getProcessName(context)
}