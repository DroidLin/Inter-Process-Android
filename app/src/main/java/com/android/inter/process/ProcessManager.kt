package com.android.inter.process

import android.content.Context
import com.android.inter.process.framework.Address
import com.android.inter.process.framework.IPCProvider
import com.android.inter.process.framework.address.provider
import java.util.concurrent.ConcurrentHashMap

object ProcessManager {

    private val context: Context
        get() = App.context

    private val providers = ConcurrentHashMap<Address, IPCProvider>()

    @JvmOverloads
    fun <T> fromMainProcess(clazz: Class<T>, uniqueKey: String? = null): T {
        return this.getProviderInstance(ProcessAddress.main)
            .serviceCreate(clazz, uniqueKey)
    }

    @JvmOverloads
    fun <T> fromBroadcastProcess(clazz: Class<T>, uniqueKey: String? = null): T {
        return this.getProviderInstance(ProcessAddress.broadcast)
            .serviceCreate(clazz, uniqueKey)
    }

    @JvmOverloads
    fun <T> fromProviderProcess(clazz: Class<T>, uniqueKey: String? = null): T {
        return this.getProviderInstance(ProcessAddress.provider)
            .serviceCreate(clazz, uniqueKey)
    }

    private fun getProviderInstance(address: Address): IPCProvider {
        return this.providers.getOrPut(address) { IPCProvider.on(address) }
    }

    object ProcessAddress {

        val main: Address
            get() = provider(context, context.getString(R.string.content_provider_main_ipc))

        val broadcast: Address
            get() = provider(context, context.getString(R.string.content_provider_lib_ipc))

        val provider: Address
            get() = provider(context, context.getString(R.string.content_provider_provider_ipc))

    }
}