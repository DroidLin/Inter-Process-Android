package com.android.inter.process.framework.connector

import android.content.Intent
import android.os.Build
import com.android.inter.process.framework.AndroidFunction
import com.android.inter.process.framework.BasicConnection
import com.android.inter.process.framework.BasicConnectionImpl
import com.android.inter.process.framework.FunctionConnectionPool
import com.android.inter.process.framework.InterProcessCenter
import com.android.inter.process.framework.InterProcessLogger
import com.android.inter.process.framework.SafeContinuation
import com.android.inter.process.framework.address.AndroidAddress
import com.android.inter.process.framework.address.AndroidAddress.Companion.toParcelableAddress
import com.android.inter.process.framework.address.ParcelableAndroidAddress
import com.android.inter.process.framework.metadata.ConnectContext
import com.android.inter.process.framework.metadata.ConnectRunningTask
import com.android.inter.process.framework.metadata.FunctionParameter
import com.android.inter.process.framework.receiverAndroidFunction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
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
    coroutineScope: CoroutineScope,
    address: AndroidAddress,
    typeOfConnection: (ConnectContext) -> Unit
): AndroidFunction {
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

    FunctionConnectionPool.useMutex(newParcelableAddress).withLock {
        try {
            InterProcessLogger.logDebug("<<<<<<<<<<<<<<<<<<<<<<<<< Do Real Connect Inner >>>>>>>>>>>>>>>>>>>>>>>>>")
            InterProcessLogger.logDebug("trying connect to remote: ${newParcelableAddress}.")
            val connectRecord = ConnectRunningTask(
                deferred = coroutineScope.async {
                    doConnectInner(
                        coroutineScope = coroutineScope,
                        destAddress = newParcelableAddress,
                        connectTimeout = 10_000,
                        typeOfConnection = typeOfConnection
                    )
                }
            )
            FunctionConnectionPool.addRecord(newParcelableAddress, connectRecord)
            return connectRecord.deferred.await().also { basicConnection ->
                FunctionConnectionPool[newParcelableAddress] = basicConnection
            }
        } finally {
            FunctionConnectionPool.removeRecord(newParcelableAddress)
            InterProcessLogger.logDebug("<<<<<<<<<<<<<<<<<<<<<<<<< Do Real Connect Inner Finished >>>>>>>>>>>>>>>>>>>>>>>>>")
        }
    }
}

internal suspend fun doConnectInner(
    coroutineScope: CoroutineScope,
    destAddress: ParcelableAndroidAddress,
    connectTimeout: Long,
    typeOfConnection: (ConnectContext) -> Unit
): AndroidFunction {
    return suspendCoroutine { continuation ->
        val safeContinuation = SafeContinuation(continuation)
//        val timeoutJob = coroutineScope.async {
//            delay(connectTimeout)
//            safeContinuation.resumeWithException(ConnectTimeoutException("failed to connect to remote due to connect timeout."))
//        }
        val androidFunction = BasicConnection::class.java.receiverAndroidFunction(
            object : BasicConnection by BasicConnectionImpl {
                override fun setConnectContext(connectContext: ConnectContext) {
//                    timeoutJob.cancel()
                    if (destAddress == connectContext.sourceAddress) {
                        safeContinuation.resume(connectContext.functionParameter.androidFunction)
                    } else safeContinuation.resumeWithException(IllegalArgumentException("unsatisfied source address: ${connectContext.sourceAddress} and destination address: ${destAddress}."))
                }
            }
        )
        val currentProcessAddress = InterProcessCenter.currentAddress as AndroidAddress
        val functionParameter = FunctionParameter(
            functionType = BasicConnection::class.java,
            androidFunction = androidFunction
        )
        val connectContext =
            ConnectContext(currentProcessAddress.toParcelableAddress(), functionParameter)
        typeOfConnection(connectContext)
    }
}