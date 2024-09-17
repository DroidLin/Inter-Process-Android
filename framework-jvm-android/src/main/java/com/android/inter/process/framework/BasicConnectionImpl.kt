package com.android.inter.process.framework

import com.android.inter.process.framework.metadata.ConnectContext

internal object BasicConnectionImpl : BasicConnection {

    override val version: Long get() = TODO("Not yet implemented")

    override fun setConnectContext(connectContext: ConnectContext) {}

    override fun call(parameter: AndroidRequest): AndroidResponse {
        val hostClass = parameter.declaredClassFullName.stringTypeConvert as Class<Any>
        val instance = objectPool.getInstance(hostClass)
        return hostClass.receiverAndroidFunction(instance).call(parameter)
    }
}