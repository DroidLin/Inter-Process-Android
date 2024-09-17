package com.android.inter.process.framework

import java.util.concurrent.ConcurrentHashMap

/**
 * @author: liuzhongao
 * @since: 2024/9/8 19:01
 */
object InterProcessCenter {

    private val interProcessComponent: MutableMap<Class<*>, InterProcessComponent<*>> = ConcurrentHashMap()

    @Volatile
    private var _address: Address? = null

    val currentAddress: Address
        get() = requireNotNull(this._address) { "address of current process not exist, have you called install yet?" }

    /**
     * init default address of current process, which will be transported to remote,
     * ensure this method be called once during the application lifecycle to avoid
     * temporary issues.
     */
    fun installAddress(address: Address) {
        if (this._address != null) {
            InterProcessLogger.logDebug("InterProcessCenter#installAddress should only be called once.")
            return
        }
        this._address = address
    }

    @JvmStatic
    fun <A : Address> installComponent(
        clazz: Class<A>,
        component: InterProcessComponent<in A>
    ) {
        if (this.interProcessComponent[clazz] != null) {
            InterProcessLogger.logDebug("duplicated installation of component, target: ${clazz}.")
        }
        this.interProcessComponent[clazz] = component
    }

    @JvmStatic
    internal fun <A : Address> findComponent(clazz: Class<A>): InterProcessComponent<A> {
        val component = this.interProcessComponent[clazz]
        if (component == null) {
            InterProcessLogger.logDebug("duplicated installation of component, target: ${clazz}.")
            throw NullPointerException("Can not find Component related to address type: ${clazz}, have you installed component yet?")
        }
        return component as InterProcessComponent<A>
    }
}