package com.android.inter.process

import android.content.Context
import com.android.inter.process.framework.IPCProvider
import com.android.inter.process.framework.address.AndroidAddress
import com.android.inter.process.framework.address.broadcast
import com.android.inter.process.framework.address.provider

object IPCStore {

    private val context: Context
        get() = App.context

    val mainProcessAddress: AndroidAddress
        get() = broadcast(context, context.getString(R.string.content_provider_main_ipc))

    val broadcastProcessAddress: AndroidAddress
        get() = provider(context, context.getString(R.string.content_provider_lib_ipc))

    val providerProcessAddress: AndroidAddress
        get() = broadcast(context, context.getString(R.string.content_provider_provider_ipc))

    val mainProvider by lazy { IPCProvider.on(mainProcessAddress) }
    val broadcastProvider by lazy { IPCProvider.on(broadcastProcessAddress) }
    val contentProviderProvider by lazy { IPCProvider.on(providerProcessAddress) }
}