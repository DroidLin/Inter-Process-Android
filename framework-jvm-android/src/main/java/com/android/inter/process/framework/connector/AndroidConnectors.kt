package com.android.inter.process.framework.connector

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.android.inter.process.framework.BasicConnection
import com.android.inter.process.framework.address.AndroidAddress
import com.android.inter.process.framework.address.AndroidAddress.Companion.packageName
import com.android.inter.process.framework.address.BroadcastAndroidAddress
import com.android.inter.process.framework.address.ContentProviderAndroidAddress
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
        is BroadcastAndroidAddress -> {
            broadcastConnectionInner(remoteAddress, connectContext)
            null
        }
        is ContentProviderAndroidAddress -> contentProviderConnectionInner(remoteAddress, connectContext)
        else -> null
    }
}

private fun broadcastConnectionInner(broadcastAddress: BroadcastAndroidAddress, connectContext: ConnectContext) {
    val intent = Intent(broadcastAddress.broadcastAction)
    intent.`package` = broadcastAddress.packageName
    intent.connectContext = connectContext
    broadcastAddress.context.sendBroadcast(intent)
}

private fun contentProviderConnectionInner(contentProviderAddress: ContentProviderAndroidAddress, connectContext: ConnectContext): ConnectContext {
    val uri = contentProviderAddress.uri
    val bundle = Bundle()
    bundle.connectContext = connectContext
    return requireNotNull(
        contentProviderAddress.context.contentResolver.call(uri, FUNCTION_CONTENT_PROVIDER_CONNECT, "", bundle)
            ?.connectContext
    )
}