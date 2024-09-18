package com.android.inter.process.framework.content

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.android.inter.process.framework.BasicConnection
import com.android.inter.process.framework.BasicConnectionImpl
import com.android.inter.process.framework.FunctionConnectionPool
import com.android.inter.process.framework.InterProcessCenter
import com.android.inter.process.framework.address.AndroidAddress
import com.android.inter.process.framework.address.AndroidAddress.Companion.toParcelableAddress
import com.android.inter.process.framework.connector.connectContext
import com.android.inter.process.framework.metadata.ConnectContext

/**
 * @author: liuzhongao
 * @since: 2024/9/17 13:26
 */
abstract class BroadcastAndroidReceiver : BroadcastReceiver() {

    final override fun onReceive(context: Context?, intent: Intent?) {
        context ?: return
        val connectContext = intent?.connectContext ?: return
        val newConnectContext = ConnectContext(
            sourceAddress = (InterProcessCenter.currentAddress as AndroidAddress).toParcelableAddress(),
            basicConnection = BasicConnection(BasicConnectionImpl)
        )
        FunctionConnectionPool[newConnectContext.sourceAddress] = connectContext.basicConnection
        connectContext.basicConnection.setConnectContext(newConnectContext)
    }
}