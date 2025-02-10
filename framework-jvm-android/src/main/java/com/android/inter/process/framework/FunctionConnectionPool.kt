package com.android.inter.process.framework

import com.android.inter.process.framework.address.ParcelableAndroidAddress
import com.android.inter.process.framework.metadata.ConnectRunningTask
import kotlinx.coroutines.sync.Mutex

/**
 * @author: liuzhongao
 * @since: 2024/9/17 03:18
 */
internal object FunctionConnectionPool {

    private val basicConnectionMutableMap: MutableMap<ParcelableAndroidAddress, BasicConnection> by lazy { HashMap() }
    private val connectionRunningRecord: MutableMap<ParcelableAndroidAddress, ConnectRunningTask> by lazy { HashMap() }
    private val mutexMap: MutableMap<ParcelableAndroidAddress, Mutex> by lazy { HashMap() }

    fun useMutex(address: ParcelableAndroidAddress): Mutex {
        if (this.mutexMap[address] == null) {
            synchronized(this.mutexMap) {
                if (this.mutexMap[address] == null) {
                    this.mutexMap[address] = Mutex()
                }
            }
        }
        return requireNotNull(this.mutexMap[address])
    }

    fun addRecord(androidAddress: ParcelableAndroidAddress, record: ConnectRunningTask) {
        synchronized(this.connectionRunningRecord) {
            this.connectionRunningRecord[androidAddress] = record
        }
    }

    fun removeRecord(androidAddress: ParcelableAndroidAddress) {
        synchronized(this.connectionRunningRecord) {
            this.connectionRunningRecord.remove(androidAddress)
        }
    }

    fun getRecord(androidAddress: ParcelableAndroidAddress): ConnectRunningTask? {
        return synchronized(this.connectionRunningRecord) {
            this.connectionRunningRecord[androidAddress]
        }
    }

    operator fun set(androidAddress: ParcelableAndroidAddress, basicConnection: BasicConnection) {
        if (this.basicConnectionMutableMap[androidAddress] == null) {
            synchronized(this.basicConnectionMutableMap) {
                if (this.basicConnectionMutableMap[androidAddress] == null) {
                    this.basicConnectionMutableMap[androidAddress] = basicConnection
                }
            }
        }
    }

    operator fun get(androidAddress: ParcelableAndroidAddress): BasicConnection? {
        return this.basicConnectionMutableMap[androidAddress]
    }

    fun getOrPut(address: ParcelableAndroidAddress, put: () -> BasicConnection): BasicConnection {
        val existInstance = this.basicConnectionMutableMap[address]
        if (existInstance != null) {
            return existInstance
        }
        val newConnection = put()
        this.basicConnectionMutableMap[address] = newConnection
        return newConnection
    }
}