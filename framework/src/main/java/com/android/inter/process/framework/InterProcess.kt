package com.android.inter.process.framework

import com.android.inter.process.framework.metadata.ServiceCreateResource

/**
 * delegate to [InterProcessComponent] by creating callers for interfaces.
 *
 * @author: liuzhongao
 * @since: 2024/9/8 18:27
 */
class InterProcess private constructor(
    private val component: InterProcessComponent<Address>,
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
        fun with(address: Address): InterProcess {
            val component = InterProcessCenter.findComponent(address.javaClass)
            return InterProcess(component, address)
        }
    }
}