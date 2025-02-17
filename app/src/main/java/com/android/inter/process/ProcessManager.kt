package com.android.inter.process

import android.content.Context
import com.android.inter.process.framework.IPCProvider
import com.android.inter.process.framework.address.AndroidAddress
import com.android.inter.process.framework.address.provider
import java.util.concurrent.ConcurrentHashMap

object ProcessManager {

    private val context: Context
        get() = App.context

    private val providers = ConcurrentHashMap<AndroidAddress, IPCProvider>()

    @JvmOverloads
    fun <T> fromMainProcess(clazz: Class<T>, uniqueKey: String? = null): T {
        return this.getProviderInstance(Address.mainProcessAddress)
            .serviceCreate(clazz, uniqueKey)
    }

    @JvmOverloads
    fun <T> fromBroadcastProcess(clazz: Class<T>, uniqueKey: String? = null): T {
        return this.getProviderInstance(Address.broadcastProcessAddress)
            .serviceCreate(clazz, uniqueKey)
    }

    @JvmOverloads
    fun <T> fromProviderProcess(clazz: Class<T>, uniqueKey: String? = null): T {
        return this.getProviderInstance(Address.providerProcessAddress)
            .serviceCreate(clazz, uniqueKey)
    }

    private fun getProviderInstance(address: AndroidAddress): IPCProvider {
        return this.providers.getOrPut(address) { IPCProvider.on(address) }
    }

    object Address {

        val mainProcessAddress: AndroidAddress
            get() = provider(context, context.getString(R.string.content_provider_main_ipc))

        val broadcastProcessAddress: AndroidAddress
            get() = provider(context, context.getString(R.string.content_provider_lib_ipc))

        val providerProcessAddress: AndroidAddress
            get() = provider(context, context.getString(R.string.content_provider_provider_ipc))

    }
}