package com.android.inter.process.framework

import com.android.inter.process.framework.metadata.InitializeConfig
import java.util.ServiceLoader
import java.util.concurrent.ConcurrentHashMap

/**
 * @author: liuzhongao
 * @since: 2024/9/8 19:01
 */
object IPCManager {

    private val component: MutableMap<Class<*>, Component<*>> = ConcurrentHashMap()

    @Volatile
    private lateinit var _address: Address

    lateinit var initializeConfig: InitializeConfig
        private set

    val currentAddress: Address
        get() = if (this::_address.isInitialized) {
            _address
        } else error("address of current process not exist, have you called install yet?")

    /**
     * init default address of current process, which will be transported to remote,
     * ensure this method be called once during the application lifecycle to avoid
     * temporary issues.
     */
    fun installAddress(address: Address) {
        if (this::_address.isInitialized) {
            Logger.logDebug("InterProcessCenter#installAddress should only be called once.")
            return
        }
        this._address = address
    }

    @JvmStatic
    fun <A : Address> installComponent(
        clazz: Class<A>,
        component: Component<in A>
    ) {
        if (this.component[clazz] != null) {
            Logger.logDebug("duplicated installation of component, target: ${clazz}.")
        }
        this.component[clazz] = component
    }

    @JvmStatic
    internal fun <A : Address> findComponent(clazz: Class<A>): Component<A> {
        val component = this.component[clazz]
        if (component == null) {
            Logger.logDebug("duplicated installation of component, target: ${clazz}.")
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