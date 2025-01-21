package com.android.inter.process

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.os.Process
import com.android.inter.process.framework.IPCManager
import com.android.inter.process.framework.address.broadcast
import com.android.inter.process.framework.installAndroid
import com.android.inter.process.framework.objectPool
import com.android.inter.process.framework.reflect.InvocationReceiverAndroid

/**
 * @author: liuzhongao
 * @since: 2024/9/17 15:39
 */
class App : Application() {

    override fun onCreate() {
        super.onCreate()

        val packageName = getProcessName(this)
        println("process name: ${packageName}.")
        if (packageName.endsWith(":lib")) { // lib进程
            val androidAddress = broadcast(context = this, broadcastAction = getString(R.string.broadcast_action_lib_ipc))
            IPCManager.installAndroid(androidAddress)
        } else if (!packageName.contains(":")) { // main进程
            val androidAddress = broadcast(context = this, broadcastAction = getString(R.string.broadcast_action_main_ipc))
            IPCManager.installAndroid(androidAddress)
        }
//        objectPool.putCallerBuilder(ApplicationInfo::class.java) { ApplicationInfoCaller(it) }
//        objectPool.putReceiverBuilder(ApplicationInfo::class.java) { ApplicationInfoReceiver(it) }
        objectPool.putReceiver(ApplicationInfo::class.java, InvocationReceiverAndroid(ProcessApplicationInfo(this)))
    }

    companion object {
        fun getProcessName(context: Context): String {
            val pid = Process.myPid()
            return kotlin.runCatching {
                (context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager)
                    ?.runningAppProcesses?.find { it.pid == pid }?.processName
            }.getOrNull() ?: ""
        }
    }
}