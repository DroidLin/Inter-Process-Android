package com.android.inter.process.framework

import com.android.inter.process.framework.reflect.InvocationReceiver
import com.android.inter.process.framework.reflect.InvocationReceiverAndroid
import java.lang.reflect.InvocationHandler
import java.util.concurrent.ConcurrentHashMap

internal class AndroidObjectPool : ObjectPool {

    private val invocationHandlerCache: MutableMap<Class<*>, InvocationHandler> = ConcurrentHashMap()

    private val callerCacheBuilders: MutableMap<Class<*>, (FunctionCallAdapter) -> Any?> = ConcurrentHashMap()
    private val receiverBuilderCache: MutableMap<Class<*>, (Any) -> InvocationReceiver<Any>> = ConcurrentHashMap()
    private val receiverCache: MutableMap<Class<*>, InvocationReceiver<*>> = ConcurrentHashMap()
    private val instanceFactoryCache: MutableMap<Class<*>, ServiceFactory<*>> = ConcurrentHashMap()

    override fun tryGetInvocationHandler(
        clazz: Class<*>,
        factory: () -> InvocationHandler
    ): InvocationHandler {
        if (this.invocationHandlerCache[clazz] == null) {
            this.invocationHandlerCache[clazz] = factory()
        }
        return requireNotNull(this.invocationHandlerCache[clazz])
    }

    override fun <T> putCallerFactory(clazz: Class<T>, callerFactory: CallerFactory<T>) {
        this.callerCacheBuilders.putIfAbsent(clazz, callerFactory)
    }

    override fun <T> getCaller(clazz: Class<T>, functionCallAdapter: FunctionCallAdapter): T? {
        return this.callerCacheBuilders[clazz]?.invoke(functionCallAdapter) as? T
    }

    override fun <T> putReceiver(clazz: Class<T>, receiver: InvocationReceiver<T>) {
        this.receiverCache.putIfAbsent(clazz, receiver)
    }

    override fun <T> getReceiver(clazz: Class<T>): InvocationReceiver<T> {
        val cacheInReceiver = this.receiverCache[clazz] as? InvocationReceiver<T>
        if (cacheInReceiver != null) {
            return cacheInReceiver
        }
        val cacheInInstanceFactory = this.instanceFactoryCache.get(clazz) as? ServiceFactory<T>
            ?: error("there is no receiver or the instance factory registered in object pool.")
        val receiverFactory = this.receiverBuilderCache.get(clazz) as? ReceiverFactory<T>
            ?: ::InvocationReceiverAndroid
        val invocationReceiver = receiverFactory(cacheInInstanceFactory.serviceCreate())
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
    }

    override fun <T> putInstanceFactory(clazz: Class<T>, serviceFactory: ServiceFactory<T>) {
        instanceFactoryCache.putIfAbsent(clazz, serviceFactory)
    }
}