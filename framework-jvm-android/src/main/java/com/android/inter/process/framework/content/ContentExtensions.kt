package com.android.inter.process.framework.content

import com.android.inter.process.framework.BasicConnection
import com.android.inter.process.framework.BasicConnectionStub
import com.android.inter.process.framework.FunctionConnectionPool
import com.android.inter.process.framework.IPCManager
import com.android.inter.process.framework.address.AndroidAddress
import com.android.inter.process.framework.address.AndroidAddress.Companion.toParcelableAddress
import com.android.inter.process.framework.metadata.ConnectContext
import com.android.inter.process.framework.processAddress

internal fun handleConnect(connectContext: ConnectContext) {
    val localConnectContext = handleConnectAndReturn(connectContext)
    FunctionConnectionPool[connectContext.sourceAddress]?.setConnectContext(localConnectContext)
}

internal fun handleConnectAndReturn(connectContext: ConnectContext): ConnectContext {
    val localSourceAddress = (processAddress as AndroidAddress).toParcelableAddress()
    val localBasicConnection = FunctionConnectionPool.getOrPut(localSourceAddress) {
        BasicConnectionStub(BasicConnection(localSourceAddress))
    }
    FunctionConnectionPool[connectContext.sourceAddress] = connectContext.basicConnection
    return ConnectContext(sourceAddress = localSourceAddress, basicConnection = localBasicConnection)
}