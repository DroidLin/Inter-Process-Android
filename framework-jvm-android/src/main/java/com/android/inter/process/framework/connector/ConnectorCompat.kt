package com.android.inter.process.framework.connector

import android.content.Intent
import android.os.Build
import android.os.Bundle
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
import com.android.inter.process.framework.isDebugMode
import com.android.inter.process.framework.metadata.ConnectContext
import com.android.inter.process.framework.metadata.ConnectRunningTask
import com.android.inter.process.framework.processAddress
import com.android.inter.process.framework.withConnectionScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.intrinsics.suspendCoroutineUninterceptedOrReturn
import kotlin.coroutines.intrinsics.COROUTINE_SUSPENDED

private const val KEY_CONNECT_CONTEXT = "key_connect_context"

internal var Intent.connectContext: ConnectContext?
    set(value) {
        this.putExtra(KEY_CONNECT_CONTEXT, value)
    }
    get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        this.getParcelableExtra(KEY_CONNECT_CONTEXT, ConnectContext::class.java)
    } else this.getParcelableExtra(KEY_CONNECT_CONTEXT)

internal var Bundle.connectContext: ConnectContext?
    set(value) {
        this.putParcelable(KEY_CONNECT_CONTEXT, value)
    }
    get() {
        classLoader = ConnectContext::class.java.classLoader
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            this.getParcelable(KEY_CONNECT_CONTEXT, ConnectContext::class.java)
        } else this.getParcelable(KEY_CONNECT_CONTEXT)
    }

internal suspend fun doConnect(
    address: AndroidAddress,
    connectTimeout: Long,
    typeOfConnection: (ConnectContext) -> ConnectContext?
): BasicConnection {
    val newParcelableAddress = address.toParcelableAddress()
    val basicConnection = FunctionConnectionPool[newParcelableAddress]
    if (basicConnection != null && basicConnection.isConnected) {
        if (isDebugMode) {
            Logger.logDebug("function already exist and alive, just return the value.")
        }
        return basicConnection
    }
    // we need to wait if there is an existing connection task.
    val firstCheckTask = FunctionConnectionPool.getRecord(newParcelableAddress)
    if (firstCheckTask != null) {
        if (isDebugMode) {
            Logger.logDebug("another connection task is running, wait until finished.")
        }
        return firstCheckTask.deferred.await()
    }
    return FunctionConnectionPool.useMutex(newParcelableAddress).withLock {
        // now we check the connection again, trying to get previous running result.
        val newBasicConnection = FunctionConnectionPool[newParcelableAddress]
        if (newBasicConnection != null && newBasicConnection.isConnected) {
            if (isDebugMode) {
                Logger.logDebug("double check function already exist and alive, just return the value.")
            }
            return@withLock newBasicConnection
        }
        // or just launch connection logics.
        withConnectionScope connectionScope@{
            try {
                if (isDebugMode) {
                    Logger.logDebug("<<<<<<<<<<<<<<<<<<<<<<<<< Do Real Connect Inner >>>>>>>>>>>>>>>>>>>>>>>>>")
                    Logger.logDebug("trying connect to remote: ${newParcelableAddress}.")
                }
                val newTask = ConnectRunningTask(
                    deferred = async {
                        doConnectInner(
                            coroutineScope = this@connectionScope,
                            destAddress = newParcelableAddress,
                            connectTimeout = connectTimeout,
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
                if (isDebugMode) {
                    Logger.logDebug("<<<<<<<<<<<<<<<<<<<<<<<<< Do Real Connect Inner Finished >>>>>>>>>>>>>>>>>>>>>>>>>")
                }
            }
        }
    }
}

internal suspend fun doConnectInner(
    coroutineScope: CoroutineScope,
    destAddress: ParcelableAndroidAddress,
    connectTimeout: Long,
    typeOfConnection: (ConnectContext) -> ConnectContext?
): BasicConnection {
    return suspendCoroutineUninterceptedOrReturn { continuation ->
        val safeContinuation = SafeContinuation(continuation)
        val timeoutJob = coroutineScope.async {
            delay(connectTimeout)
            safeContinuation.resumeWithException(ConnectTimeoutException("failed to connect to remote due to connect timeout."))
        }
        val sourceAddress = (processAddress as AndroidAddress).toParcelableAddress()
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
        val remoteConnectContext = typeOfConnection(connectContext)
        if (remoteConnectContext != null) {
            timeoutJob.cancel()
            if (remoteConnectContext.sourceAddress == destAddress) {
                remoteConnectContext.basicConnection
            } else throw IllegalArgumentException("unsatisfied source address: ${sourceAddress} and destination address: ${destAddress}.")
        } else COROUTINE_SUSPENDED
    }
}