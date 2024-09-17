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
import com.android.inter.process.framework.metadata.FunctionParameter
import com.android.inter.process.framework.metadata.function
import com.android.inter.process.framework.objectPool
import com.android.inter.process.framework.receiverAndroidFunction

/**
 * @author: liuzhongao
 * @since: 2024/9/17 13:26
 */
abstract class BroadcastAndroidReceiver : BroadcastReceiver() {

    final override fun onReceive(context: Context?, intent: Intent?) {
        context ?: return
        val connectContext = intent?.connectContext ?: return
        val androidFunction = BasicConnection::class.java.receiverAndroidFunction(BasicConnectionImpl)
        val newConnectContext = ConnectContext(
            sourceAddress = (InterProcessCenter.currentAddress as AndroidAddress).toParcelableAddress(),
            functionParameter = FunctionParameter(
                functionType = BasicConnection::class.java,
                androidFunction = androidFunction
            )
        )
        FunctionConnectionPool[newConnectContext.sourceAddress] = connectContext.functionParameter.androidFunction
        BasicConnection(connectContext.functionParameter.androidFunction).setConnectContext(newConnectContext)
    }
}