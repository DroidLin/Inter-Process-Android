package com.android.inter.process.framework.connector

import android.content.Intent
import android.os.Build
import com.android.inter.process.framework.BasicConnection
import com.android.inter.process.framework.BasicConnectionImpl
import com.android.inter.process.framework.FunctionConnectionPool
import com.android.inter.process.framework.InterProcessCenter
import com.android.inter.process.framework.InterProcessLogger
import com.android.inter.process.framework.SafeContinuation
import com.android.inter.process.framework.address.AndroidAddress
import com.android.inter.process.framework.address.AndroidAddress.Companion.toParcelableAddress
import com.android.inter.process.framework.address.ParcelableAndroidAddress
import com.android.inter.process.framework.exceptions.ConnectTimeoutException
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
    if (basicConnection != null) {
        InterProcessLogger.logDebug("android function already exist and alive, just return the value.")
        return basicConnection
    }

    val runningTask = FunctionConnectionPool.getRecord(newParcelableAddress)
    if (runningTask != null) {
        InterProcessLogger.logDebug("another connection task is running, wait until finished.")
        return runningTask.deferred.await()
    }

    return withConnectionScope connectionScope@{
        FunctionConnectionPool.useMutex(newParcelableAddress).withLock {
            try {
                InterProcessLogger.logDebug("<<<<<<<<<<<<<<<<<<<<<<<<< Do Real Connect Inner >>>>>>>>>>>>>>>>>>>>>>>>>")
                InterProcessLogger.logDebug("trying connect to remote: ${newParcelableAddress}.")
                val connectRecord = ConnectRunningTask(
                    deferred = async {
                        doConnectInner(
                            coroutineScope = this@connectionScope,
                            destAddress = newParcelableAddress,
                            connectTimeout = 10_000,
                            typeOfConnection = typeOfConnection
                        )
                    }
                )
                FunctionConnectionPool.addRecord(newParcelableAddress, connectRecord)
                connectRecord.deferred.await().also { basicConnection ->
                    FunctionConnectionPool[newParcelableAddress] = basicConnection
                }
            } finally {
                FunctionConnectionPool.removeRecord(newParcelableAddress)
                InterProcessLogger.logDebug("<<<<<<<<<<<<<<<<<<<<<<<<< Do Real Connect Inner Finished >>>>>>>>>>>>>>>>>>>>>>>>>")
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
        val basicConnection = BasicConnection(
            object : BasicConnection by BasicConnectionImpl {
                override fun setConnectContext(connectContext: ConnectContext) {
                    timeoutJob.cancel()
                    if (destAddress == connectContext.sourceAddress) {
                        safeContinuation.resume(connectContext.basicConnection)
                    } else safeContinuation.resumeWithException(IllegalArgumentException("unsatisfied source address: ${connectContext.sourceAddress} and destination address: ${destAddress}."))
                }
            }
        )
        val connectContext = ConnectContext(
            sourceAddress = (InterProcessCenter.currentAddress as AndroidAddress).toParcelableAddress(),
            basicConnection = basicConnection
        )
        typeOfConnection(connectContext)
    }
}