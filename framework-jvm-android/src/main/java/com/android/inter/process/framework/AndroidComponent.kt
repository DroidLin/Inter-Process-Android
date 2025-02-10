package com.android.inter.process.framework

import com.android.inter.process.framework.address.AndroidAddress
import com.android.inter.process.framework.connector.AndroidConnectorHandle
import com.android.inter.process.framework.connector.functionAndroidConnectionHandle
import com.android.inter.process.framework.metadata.ServiceCreateResource
import com.android.inter.process.framework.reflect.InvocationCallerAndroid
import com.android.inter.process.framework.reflect.callerFunction

/**
 * implementation of Component, used to create android-based inter process
 * call logic.
 *
 * @author: liuzhongao
 * @since: 2024/9/13 00:23
 */
internal class AndroidComponent : Component<AndroidAddress> {

    override fun <T : Any> serviceCreate(serviceCreateResource: ServiceCreateResource<T, AndroidAddress>): T {
        // if we are going to connect to local process, just skip and return the target instance.
        if (serviceCreateResource.interProcessAddress == IPCManager.currentAddress) {
            return objectPool.getInstance(serviceCreateResource.clazz)
        }
        val functionCall = FunctionCallAdapter {
            val address = serviceCreateResource.interProcessAddress
            val connector = AndroidConnectorHandle(address, ::functionAndroidConnectionHandle)
            AndroidFunctionCallAdapter(connector.tryConnect())
        }
        val clazz = serviceCreateResource.clazz
        return objectPool.getCaller(clazz = clazz, functionCallAdapter = functionCall)
            ?: clazz.callerFunction(InvocationCallerAndroid(functionCallAdapter = functionCall))
    }
}