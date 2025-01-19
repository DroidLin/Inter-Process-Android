package com.android.inter.process.framework

import com.android.inter.process.framework.metadata.ServiceCreateResource

/**
 * delegate to [Component] by creating callers for interfaces.
 *
 * @author: liuzhongao
 * @since: 2024/9/8 18:27
 */
class IPCProvider private constructor(
    private val component: Component<Address>,
    private val address: Address,
) {

    fun <T : Any> serviceCreate(clazz: Class<T>): T {
        if (!clazz.isInterface) {
            throw IllegalArgumentException("class: $clazz is not declared as an interface.")
        }
        val resource = ServiceCreateResource(
            clazz = clazz,
            interProcessAddress = this.address,
        )
        return this.component.serviceCreate(serviceCreateResource = resource)
    }

    companion object {

        @JvmStatic
        fun on(address: Address): IPCProvider {
            val component = IPCManager.findComponent(address.javaClass)
            return IPCProvider(component, address)
        }
    }
}