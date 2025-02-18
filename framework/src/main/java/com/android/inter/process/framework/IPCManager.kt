package com.android.inter.process.framework

import com.android.inter.process.framework.metadata.InitConfig
import java.util.ServiceLoader
import java.util.concurrent.ConcurrentHashMap

val isDebugMode: Boolean get() = IPCManager.initConfig.debugMode

val processAddress: Address get() = IPCManager.initConfig.initAddress

val connectTimeout: Long get() = IPCManager.initConfig.connectTimeout


/**
 * @author: liuzhongao
 * @since: 2024/9/8 19:01
 */
object IPCManager {

    private val component: MutableMap<Class<*>, Component<*>> = ConcurrentHashMap()

    @Volatile
    lateinit var initConfig: InitConfig
        private set

    fun installConfig(config: InitConfig) {
        if (this::initConfig.isInitialized) {
            if (isDebugMode) {
                Logger.logDebug("InterProcessCenter#install should only be called once.")
            }
            return
        }
        this.initConfig = config
    }

    @JvmStatic
    fun <A : Address> installComponent(
        clazz: Class<A>,
        component: Component<in A>
    ) {
        if (this.component[clazz] != null) {
            if (isDebugMode) {
                Logger.logDebug("duplicated installation of component, target: ${clazz}, component: ${component.javaClass.name}.")
            }
        }
        this.component[clazz] = component
    }

    @JvmStatic
    internal fun <A : Address> findComponent(clazz: Class<A>): Component<A> {
        val component = this.component[clazz]
        if (component == null) {
            throw NullPointerException("Can not find Component related to address type: ${clazz}, have you installed component yet?")
        }
        return component as Component<A>
    }

    @JvmStatic
    fun installDependencies() {
        kotlin.runCatching {
            ServiceLoader.load(ObjectPool.Collector::class.java)
                .forEach(ObjectPool.Collector::collect)
        }.onFailure { Logger.logError(it) }
    }
}