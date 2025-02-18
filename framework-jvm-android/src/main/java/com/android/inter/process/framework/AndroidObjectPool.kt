package com.android.inter.process.framework

import com.android.inter.process.framework.exceptions.ImplementationNotFoundException
import com.android.inter.process.framework.reflect.InvocationReceiver
import com.android.inter.process.framework.reflect.InvocationReceiverAndroid
import java.util.concurrent.ConcurrentHashMap

internal class AndroidObjectPool : ObjectPool {

    private val callerBuilderCache: MutableMap<Class<*>, CallerFactory<Any?>> = ConcurrentHashMap()
    private val receiverBuilderCache: MutableMap<Class<*>, ReceiverFactory<Any>> = ConcurrentHashMap()

    private val receiverCache: MutableMap<String, InvocationReceiver<*>> = ConcurrentHashMap()

    private val instanceFactoryCache: MutableMap<String, ServiceFactory<*>> = ConcurrentHashMap()
    private val instanceCache: MutableMap<String, Any> = ConcurrentHashMap()

    override fun <T> putCallerFactory(clazz: Class<T>, callerFactory: CallerFactory<T>) {
        this.callerBuilderCache.putIfAbsent(clazz, callerFactory)
    }

    override fun <T> getCaller(clazz: Class<T>, functionCallAdapter: FunctionCallAdapter): T? {
        return this.getCaller(clazz, null, functionCallAdapter)
    }

    override fun <T> getCaller(clazz: Class<T>, uniqueKey: String?, functionCallAdapter: FunctionCallAdapter): T? {
        return this.callerBuilderCache[clazz]?.invoke(functionCallAdapter, uniqueKey) as? T
    }

    override fun <T> putReceiver(clazz: Class<T>, receiver: InvocationReceiver<T>) {
        this.putReceiver(clazz, null, receiver)
    }

    override fun <T> putReceiver(clazz: Class<T>, uniqueKey: String?, receiver: InvocationReceiver<T>) {
        val key = calculateUniqueKeys(clazz, uniqueKey)
        this.receiverCache.putIfAbsent(key, receiver)
    }

    override fun <T> getReceiver(clazz: Class<T>): InvocationReceiver<T> {
        return this.getReceiver(clazz, null)
    }

    override fun <T> getReceiver(clazz: Class<T>, uniqueKey: String?): InvocationReceiver<T> {
        val key = calculateUniqueKeys(clazz, uniqueKey)
        val cacheInReceiver = this.receiverCache[key] as? InvocationReceiver<T>
        if (cacheInReceiver != null) {
            return cacheInReceiver
        }
        val serviceInstance = this.getInstance(clazz, uniqueKey)
        val invocationReceiver =
            (this.receiverBuilderCache.getOrPut(clazz) { ::InvocationReceiverAndroid } as ReceiverFactory<T>)
                .invoke(serviceInstance)
        this.putReceiver(clazz, uniqueKey, invocationReceiver)
        return invocationReceiver
    }

    override fun <T> putReceiverFactory(clazz: Class<T>, receiverFactory: ReceiverFactory<T>) {
        this.receiverBuilderCache.putIfAbsent(clazz, receiverFactory as ReceiverFactory<Any>)
    }

    override fun <T> getReceiverBuilder(clazz: Class<T>): ReceiverFactory<T>? {
        return this.receiverBuilderCache[clazz] as? ReceiverFactory<T>
    }

    override fun <T> putInstanceFactory(clazz: Class<T>, serviceFactory: ServiceFactory<T>) {
        putInstanceFactory(clazz, null, serviceFactory)
    }

    override fun <T> putInstanceFactory(clazz: Class<T>, uniqueKey: String?, serviceFactory: ServiceFactory<T>) {
        val key = calculateUniqueKeys(clazz, uniqueKey)
        this.instanceFactoryCache.putIfAbsent(key, serviceFactory)
    }

    override fun <T> getInstance(clazz: Class<T>): T {
        return this.getInstance(clazz, null)
    }

    override fun <T> getInstance(clazz: Class<T>, uniqueKey: String?): T {
        val key = calculateUniqueKeys(clazz, uniqueKey)
        return this.instanceCache.getOrPut(key) {
            val cacheInInstanceFactory = this.instanceFactoryCache[key] as? ServiceFactory<T>
                ?: throw ImplementationNotFoundException("there is no receiver or the instance $clazz factory registered in object pool.")
            cacheInInstanceFactory.serviceCreate() as Any
        } as T
    }

    private fun calculateUniqueKeys(clazz: Class<*>, uniqueKey: String?): String {
        return if (uniqueKey.isNullOrEmpty() || uniqueKey.isBlank()) {
            clazz.name
        } else {
            "${clazz.name}_$uniqueKey"
        }
    }
}