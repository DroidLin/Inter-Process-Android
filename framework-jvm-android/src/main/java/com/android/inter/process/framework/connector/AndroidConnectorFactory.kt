package com.android.inter.process.framework.connector

import com.android.inter.process.framework.Connector
import com.android.inter.process.framework.address.AndroidAddress
import com.android.inter.process.framework.address.BroadcastAndroidAddress

/**
 * @author: liuzhongao
 * @since: 2024/9/16 19:17
 */
internal object AndroidConnectorFactory : Connector.Factory<AndroidAddress, AndroidConnector<AndroidAddress>> {

    override fun connectorCreate(address: AndroidAddress): AndroidConnector<AndroidAddress> {
        return when (address) {
            is BroadcastAndroidAddress -> BroadcastAndroidConnector(address)
            else -> throw IllegalArgumentException("unknown android address: ${address.javaClass}")
        } as AndroidConnector<AndroidAddress>
    }
}