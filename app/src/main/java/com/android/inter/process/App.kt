package com.android.inter.process

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.os.Process
import android.os.StrictMode
import com.android.inter.process.framework.IPCManager
import com.android.inter.process.framework.install
import com.android.inter.process.framework.metadata.InitConfig
import java.lang.ref.WeakReference


/**
 * @author: liuzhongao
 * @since: 2024/9/17 15:39
 */
class App : Application() {

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        context = this

        val packageName = getProcessName(this)
        println("process name: ${packageName}.")
        if (packageName.endsWith(":broadcast")) { // broadcast进程
            IPCManager.install(InitConfig(ProcessManager.ProcessAddress.broadcast))
        } else if (packageName.endsWith(":provider")) { // provider进程
            IPCManager.install(InitConfig(ProcessManager.ProcessAddress.provider))
        } else if (!packageName.contains(":")) { // main进程
            IPCManager.install(InitConfig(ProcessManager.ProcessAddress.main))
        }

        StrictMode.enableDefaults()

        Class.forName("dalvik.system.CloseGuard")
            .getMethod("setEnabled", Boolean::class.javaPrimitiveType)
            .invoke(null, true)
    }

    companion object {
        private var contextRef: WeakReference<Context>? = null
        var context: Context
            get() = requireNotNull(contextRef?.get())
            private set(value) {
                this.contextRef = WeakReference(value)
            }

        fun getProcessName(context: Context): String {
            val pid = Process.myPid()
            return kotlin.runCatching {
                (context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager)
                    ?.runningAppProcesses?.find { it.pid == pid }?.processName
            }.getOrNull() ?: ""
        }
    }
}