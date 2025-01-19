package com.android.inter.process.framework

import com.android.inter.process.framework.reflect.InvocationReceiver
import java.lang.reflect.InvocationHandler
import java.util.concurrent.ConcurrentHashMap

/**
 * @author: liuzhongao
 * @since: 2024/9/8 23:34
 */
val objectPool: ObjectPool by lazy { object : ObjectPool by IPCObjectPool {} }

internal object IPCObjectPool : ObjectPool {

    private val invocationHandlerCache: MutableMap<Class<*>, InvocationHandler> = ConcurrentHashMap()

    private val callerCacheBuilders: MutableMap<Class<*>, (FunctionCallTransformer) -> Any?> = ConcurrentHashMap()
    private val receiverBuilderCache: MutableMap<Class<*>, (Any) -> InvocationReceiver<Any>> = ConcurrentHashMap()
    private val receiverCache: MutableMap<Class<*>, InvocationReceiver<*>> = ConcurrentHashMap()

    fun tryGetInvocationHandler(
        clazz: Class<*>,
        factory: () -> InvocationHandler
    ): InvocationHandler {
        if (this.invocationHandlerCache[clazz] == null) {
            this.invocationHandlerCache[clazz] = factory()
        }
        return requireNotNull(this.invocationHandlerCache[clazz])
    }

    override fun <T : Any> putCallerBuilder(clazz: Class<T>, callerBuilder: CallerBuilder<T>) {
        this.callerCacheBuilders.putIfAbsent(clazz, callerBuilder)
    }

    override fun <T : Any> getCaller(clazz: Class<T>, commander: FunctionCallTransformer): T? {
        return this.callerCacheBuilders[clazz]?.invoke(commander) as? T
    }

    override fun <T : Any> putReceiver(clazz: Class<T>, receiver: InvocationReceiver<T>) {
        this.receiverCache.putIfAbsent(clazz, receiver)
    }

    override fun <T : Any> getReceiver(clazz: Class<T>): InvocationReceiver<T> {
        return requireNotNull(this.receiverCache[clazz] as? InvocationReceiver<T>)
    }

    override fun <T : Any> putReceiverBuilder(clazz: Class<T>, receiverBuilder: ReceiverBuilder<T>) {
        this.receiverBuilderCache.putIfAbsent(clazz, receiverBuilder as ReceiverBuilder<Any>)
    }

    override fun <T : Any> getReceiverBuilder(clazz: Class<T>): ReceiverBuilder<T>? {
        return this.receiverBuilderCache[clazz] as? ReceiverBuilder<T>
    }

    override fun <T : Any> putInstance(clazz: Class<T>, instance: T) {
        val receiverBuilder = this.receiverBuilderCache.get(clazz)
        if (receiverBuilder != null) {
            val invocationReceiver = receiverBuilder(instance)
            this.putReceiver(clazz, invocationReceiver as InvocationReceiver<T>)
            return
        }
        this.putReceiver(clazz, InvocationReceiver(instance = instance))
    }

}