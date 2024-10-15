package com.android.inter.process.framework.connector

import android.content.Intent
import com.android.inter.process.framework.AndroidFunction
import com.android.inter.process.framework.BasicConnection
import com.android.inter.process.framework.address.BroadcastAndroidAddress
import com.android.inter.process.framework.withConnectionScope

/**
 * @author: liuzhongao
 * @since: 2024/9/16 15:33
 */
internal class BroadcastAndroidConnector(
    private val address: BroadcastAndroidAddress
) : AndroidConnector<BroadcastAndroidAddress> {

    override suspend fun tryConnect(): BasicConnection {
        val address = this.address
        return doConnect(address) { connectContext ->
            val intent = Intent(address.broadcastAction)
            intent.`package` = address.packageName
            intent.connectContext = connectContext
            address.context.sendBroadcast(intent)
        }
    }
}