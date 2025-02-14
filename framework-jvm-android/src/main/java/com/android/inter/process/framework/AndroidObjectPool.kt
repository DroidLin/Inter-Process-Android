package com.android.inter.process.framework

import com.android.inter.process.framework.reflect.InvocationReceiver
import com.android.inter.process.framework.reflect.InvocationReceiverAndroid
import java.lang.reflect.InvocationHandler
import java.util.concurrent.ConcurrentHashMap

internal class AndroidObjectPool : ObjectPool {

    private val callerBuilderCache: MutableMap<Class<*>, CallerFactory<Any?>> = ConcurrentHashMap()
    private val receiverBuilderCache: MutableMap<Class<*>, ReceiverFactory<Any>> = ConcurrentHashMap()

    private val receiverCache: MutableMap<Class<*>, InvocationReceiver<*>> = ConcurrentHashMap()

    private val instanceFactoryCache: MutableMap<Class<*>, ServiceFactory<*>> = ConcurrentHashMap()
    private val instanceCache: MutableMap<Class<*>, Any> = ConcurrentHashMap()

    override fun <T> putCallerFactory(clazz: Class<T>, callerFactory: CallerFactory<T>) {
        this.callerBuilderCache.putIfAbsent(clazz, callerFactory)
    }

    override fun <T> getCaller(clazz: Class<T>, functionCallAdapter: FunctionCallAdapter): T? {
        return this.callerBuilderCache[clazz]?.invoke(functionCallAdapter) as? T
    }

    override fun <T> putReceiver(clazz: Class<T>, receiver: InvocationReceiver<T>) {
        this.receiverCache.putIfAbsent(clazz, receiver)
    }

    override fun <T> getReceiver(clazz: Class<T>): InvocationReceiver<T> {
        val cacheInReceiver = this.receiverCache[clazz] as? InvocationReceiver<T>
        if (cacheInReceiver != null) {
            return cacheInReceiver
        }
        val serviceInstance = this.getInstance(clazz)
        val invocationReceiver =
            (this.receiverBuilderCache.getOrPut(clazz) { ::InvocationReceiverAndroid } as ReceiverFactory<T>)
                .invoke(serviceInstance)
        this.putReceiver(clazz, invocationReceiver)
        return invocationReceiver
    }

    override fun <T> putReceiverFactory(clazz: Class<T>, receiverFactory: ReceiverFactory<T>) {
        this.receiverBuilderCache.putIfAbsent(clazz, receiverFactory as ReceiverFactory<Any>)
    }

    override fun <T> getReceiverBuilder(clazz: Class<T>): ReceiverFactory<T>? {
        return this.receiverBuilderCache[clazz] as? ReceiverFactory<T>
    }

    override fun <T> putInstance(clazz: Class<T>, instance: T) {
        this.putInstance(clazz, instance, ::InvocationReceiverAndroid)
    }

    override fun <T> putInstance(
        clazz: Class<T>,
        instance: T,
        receiverFactory: ReceiverFactory<T>
    ) {
        val receiverBuilder = this.receiverBuilderCache.get(clazz)
        if (receiverBuilder != null) {
            val invocationReceiver = receiverBuilder(instance as Any)
            this.putReceiver(clazz, invocationReceiver as InvocationReceiver<T>)
            return
        }
        this.putReceiver(clazz, receiverFactory(instance))
        this.instanceCache[clazz] = instance as Any
    }

    override fun <T> putInstanceFactory(clazz: Class<T>, serviceFactory: ServiceFactory<T>) {
        instanceFactoryCache.putIfAbsent(clazz, serviceFactory)
    }

    override fun <T> getInstance(clazz: Class<T>): T {
        return this.instanceCache.getOrPut(clazz) {
            val cacheInInstanceFactory = this.instanceFactoryCache.get(clazz) as? ServiceFactory<T>
                ?: error("there is no receiver or the instance ${clazz} factory registered in object pool.")
            cacheInInstanceFactory.serviceCreate() as Any
        } as T
    }
}