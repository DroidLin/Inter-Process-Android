package com.android.inter.process.framework.connector

import com.android.inter.process.framework.AndroidFunction
import com.android.inter.process.framework.Connector
import com.android.inter.process.framework.address.AndroidAddress

/**
 * @author: liuzhongao
 * @since: 2024/9/16 15:32
 */
internal interface AndroidConnector<A : AndroidAddress> : Connector<A, AndroidFunction> {

    override suspend fun tryConnect(): AndroidFunction
}