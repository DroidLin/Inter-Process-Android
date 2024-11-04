package com.android.inter.process.framework.connector

import android.content.Intent
import com.android.inter.process.framework.BasicConnection
import com.android.inter.process.framework.address.AndroidAddress
import com.android.inter.process.framework.address.BroadcastAndroidAddress
import com.android.inter.process.framework.metadata.ConnectContext

internal fun AndroidConnectorHandle(
    androidAddress: AndroidAddress,
    functionHandle: (AndroidAddress, ConnectContext) -> Unit
): AndroidConnector<AndroidAddress> = object : AndroidConnector<AndroidAddress> {
    override val address: AndroidAddress get() = androidAddress
    override suspend fun tryConnect(): BasicConnection =
        doConnect(this.address) { functionHandle(this.address, it) }
}

internal fun functionAndroidConnectionHandle(
    remoteAddress: AndroidAddress,
    connectContext: ConnectContext
) {
    when (remoteAddress) {
        is BroadcastAndroidAddress -> broadcastConnectionInner(remoteAddress, connectContext)
        else -> {}
    }
}

private fun broadcastConnectionInner(broadcastAddress: BroadcastAndroidAddress, connectContext: ConnectContext) {
    val intent = Intent(broadcastAddress.broadcastAction)
    intent.`package` = broadcastAddress.packageName
    intent.connectContext = connectContext
    broadcastAddress.context.sendBroadcast(intent)
}