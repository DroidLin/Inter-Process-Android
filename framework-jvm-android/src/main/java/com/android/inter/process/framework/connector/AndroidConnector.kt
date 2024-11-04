package com.android.inter.process.framework.connector

import com.android.inter.process.framework.BasicConnection
import com.android.inter.process.framework.Connector
import com.android.inter.process.framework.address.AndroidAddress

/**
 * @author: liuzhongao
 * @since: 2024/9/16 15:32
 */
internal interface AndroidConnector<A : AndroidAddress> : Connector<A, BasicConnection> {

    val address: A

    override suspend fun tryConnect(): BasicConnection
}