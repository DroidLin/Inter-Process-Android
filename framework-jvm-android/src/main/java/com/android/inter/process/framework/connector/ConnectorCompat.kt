package com.android.inter.process.framework.connector

import android.content.Intent
import android.os.Build
import com.android.inter.process.framework.BasicConnection
import com.android.inter.process.framework.BasicConnectionStub
import com.android.inter.process.framework.FunctionConnectionPool
import com.android.inter.process.framework.IPCManager
import com.android.inter.process.framework.Logger
import com.android.inter.process.framework.SafeContinuation
import com.android.inter.process.framework.address.AndroidAddress
import com.android.inter.process.framework.address.AndroidAddress.Companion.toParcelableAddress
import com.android.inter.process.framework.address.ParcelableAndroidAddress
import com.android.inter.process.framework.exceptions.ConnectTimeoutException
import com.android.inter.process.framework.isConnected
import com.android.inter.process.framework.metadata.ConnectContext
import com.android.inter.process.framework.metadata.ConnectRunningTask
import com.android.inter.process.framework.withConnectionScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * @author: liuzhongao
 * @since: 2024/9/16 18:00
 */

private const val KEY_CONNECT_CONTEXT = "key_connect_context"

internal var Intent.connectContext: ConnectContext?
    set(value) {
        this.putExtra(KEY_CONNECT_CONTEXT, value)
    }
    get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        this.getParcelableExtra(KEY_CONNECT_CONTEXT, ConnectContext::class.java)
    } else this.getParcelableExtra(KEY_CONNECT_CONTEXT)

internal suspend fun doConnect(
    address: AndroidAddress,
    typeOfConnection: (ConnectContext) -> Unit
): BasicConnection {
    val newParcelableAddress = address.toParcelableAddress()
    val basicConnection = FunctionConnectionPool[newParcelableAddress]
    if (basicConnection != null && basicConnection.isConnected) {
        Logger.logDebug("android function already exist and alive, just return the value.")
        return basicConnection
    }
    // we need to wait if there is an existing task.
    val firstCheckTask = FunctionConnectionPool.getRecord(newParcelableAddress)
    if (firstCheckTask != null) {
        Logger.logDebug("another connection task is running, wait until finished.")
        return firstCheckTask.deferred.await()
    }
    return FunctionConnectionPool.useMutex(newParcelableAddress).withLock {
        // now we check the connection again.
        val newBasicConnection = FunctionConnectionPool[newParcelableAddress]
        if (newBasicConnection != null && newBasicConnection.isConnected) {
            Logger.logDebug("double check android function already exist and alive, just return the value.")
            return@withLock newBasicConnection
        }
        // or just launch connection logics.
        withConnectionScope connectionScope@{
            try {
                Logger.logDebug("<<<<<<<<<<<<<<<<<<<<<<<<< Do Real Connect Inner >>>>>>>>>>>>>>>>>>>>>>>>>")
                Logger.logDebug("trying connect to remote: ${newParcelableAddress}.")
                val newTask = ConnectRunningTask(
                    deferred = async {
                        doConnectInner(
                            coroutineScope = this@connectionScope,
                            destAddress = newParcelableAddress,
                            connectTimeout = 10_000,
                            typeOfConnection = typeOfConnection
                        )
                    }
                )
                FunctionConnectionPool.addRecord(newParcelableAddress, newTask)

                val newConnection = newTask.deferred.await()
                FunctionConnectionPool[newParcelableAddress] = newConnection
                newConnection
            } finally {
                FunctionConnectionPool.removeRecord(newParcelableAddress)
                Logger.logDebug("<<<<<<<<<<<<<<<<<<<<<<<<< Do Real Connect Inner Finished >>>>>>>>>>>>>>>>>>>>>>>>>")
            }
        }
    }
}

internal suspend fun doConnectInner(
    coroutineScope: CoroutineScope,
    destAddress: ParcelableAndroidAddress,
    connectTimeout: Long,
    typeOfConnection: (ConnectContext) -> Unit
): BasicConnection {
    return suspendCoroutine { continuation ->
        val safeContinuation = SafeContinuation(continuation)
        val timeoutJob = coroutineScope.async {
            delay(connectTimeout)
            safeContinuation.resumeWithException(ConnectTimeoutException("failed to connect to remote due to connect timeout."))
        }
        val sourceAddress = (IPCManager.currentAddress as AndroidAddress).toParcelableAddress()
        val basicConnection = BasicConnectionStub(
            object : BasicConnection by BasicConnection(sourceAddress) {
                override fun setConnectContext(connectContext: ConnectContext) {
                    timeoutJob.cancel()
                    if (destAddress == connectContext.sourceAddress) {
                        safeContinuation.resume(connectContext.basicConnection)
                    } else safeContinuation.resumeWithException(IllegalArgumentException("unsatisfied source address: ${connectContext.sourceAddress} and destination address: ${destAddress}."))
                }
            }
        )
        val connectContext = ConnectContext(
            sourceAddress = sourceAddress,
            basicConnection = basicConnection
        )
        typeOfConnection(connectContext)
    }
}