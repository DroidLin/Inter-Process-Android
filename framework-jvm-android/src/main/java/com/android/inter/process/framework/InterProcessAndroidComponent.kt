package com.android.inter.process.framework

import com.android.inter.process.framework.address.AndroidAddress
import com.android.inter.process.framework.connector.AndroidConnectorHandle
import com.android.inter.process.framework.connector.functionAndroidConnectionHandle
import com.android.inter.process.framework.metadata.ServiceCreateResource
import com.android.inter.process.framework.reflect.InvocationCaller
import com.android.inter.process.framework.reflect.callerFunction

/**
 * implementation of Component, used to create android-based inter process
 * call logic.
 *
 * @author: liuzhongao
 * @since: 2024/9/13 00:23
 */
internal class InterProcessAndroidComponent : InterProcessComponent<AndroidAddress> {

    override fun <T : Any> serviceCreate(serviceCreateResource: ServiceCreateResource<T, AndroidAddress>): T {
        val commander = ConnectionCommander {
            val address = serviceCreateResource.interProcessAddress
            val connector = AndroidConnectorHandle(address, ::functionAndroidConnectionHandle)
            AndroidAutoConnectionCommander(connector.tryConnect())
        }
        val newGeneratedInstance = objectPool.getCaller(
            clazz = serviceCreateResource.clazz,
            commander = commander
        )
        return newGeneratedInstance ?: serviceCreateResource.clazz.callerFunction(
            invocationCaller = InvocationCaller(connectionCommander = commander)
        )
    }
}