package com.android.inter.process.framework.connector

import android.content.Intent
import android.os.Bundle
import com.android.inter.process.framework.BasicConnection
import com.android.inter.process.framework.address.AndroidAddress
import com.android.inter.process.framework.address.AndroidAddress.Companion.packageName
import com.android.inter.process.framework.address.BroadcastAddress
import com.android.inter.process.framework.address.ContentProviderAddress
import com.android.inter.process.framework.metadata.ConnectContext

internal const val FUNCTION_CONTENT_PROVIDER_CONNECT = "function_content_provider_connect"

internal fun AndroidConnectorHandle(
    androidAddress: AndroidAddress,
    connectTimeout: Long,
    functionHandle: (AndroidAddress, ConnectContext) -> ConnectContext?
): AndroidConnector<AndroidAddress> = object : AndroidConnector<AndroidAddress> {
    override val address: AndroidAddress get() = androidAddress
    override suspend fun tryConnect(): BasicConnection =
        doConnect(this.address, connectTimeout) { functionHandle(this.address, it) }
}

internal fun functionAndroidConnectionHandle(
    remoteAddress: AndroidAddress,
    connectContext: ConnectContext
): ConnectContext? {
    return when (remoteAddress) {
        is BroadcastAddress -> {
            broadcastConnectionInner(remoteAddress, connectContext)
            null
        }
        is ContentProviderAddress -> contentProviderConnectionInner(remoteAddress, connectContext)
        else -> null
    }
}

private fun broadcastConnectionInner(broadcastAddress: BroadcastAddress, connectContext: ConnectContext) {
    val intent = Intent(broadcastAddress.broadcastAction)
    intent.`package` = broadcastAddress.packageName
    intent.connectContext = connectContext
    broadcastAddress.context.sendBroadcast(intent)
}

private fun contentProviderConnectionInner(contentProviderAddress: ContentProviderAddress, connectContext: ConnectContext): ConnectContext {
    val uri = contentProviderAddress.uri
    val bundle = Bundle()
    bundle.connectContext = connectContext
    val targetContext = contentProviderAddress.context
        .contentResolver
        .call(uri, FUNCTION_CONTENT_PROVIDER_CONNECT, null, bundle)
        ?.connectContext
    return requireNotNull(targetContext)
}