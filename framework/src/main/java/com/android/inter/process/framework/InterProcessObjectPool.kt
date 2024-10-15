package com.android.inter.process.framework

import com.android.inter.process.framework.reflect.DefaultInvocationReceiver
import com.android.inter.process.framework.reflect.InvocationReceiver
import java.lang.reflect.InvocationHandler
import java.util.concurrent.ConcurrentHashMap

/**
 * @author: liuzhongao
 * @since: 2024/9/8 23:34
 */
val objectPool: ObjectPool by lazy { object : ObjectPool by InterProcessObjectPool {} }

internal object InterProcessObjectPool : ObjectPool {

    private val invocationHandlerCache: MutableMap<Class<*>, InvocationHandler> =
        ConcurrentHashMap()

    private val interProcessCallerCacheBuilders: MutableMap<Class<*>, (Address) -> Any?> =
        ConcurrentHashMap()
    private val interProcessReceiverCache: MutableMap<Class<*>, InvocationReceiver<*>> =
        ConcurrentHashMap()

    fun tryGetInvocationHandler(
        clazz: Class<*>,
        factory: () -> InvocationHandler
    ): InvocationHandler {
        if (this.invocationHandlerCache[clazz] == null) {
            this.invocationHandlerCache[clazz] = factory()
        }
        return requireNotNull(this.invocationHandlerCache[clazz])
    }

    override fun <T : Any> putCallerBuilder(clazz: Class<T>, callerBuilder: (Address) -> T) {
        if (this.interProcessCallerCacheBuilders[clazz] == null) {
            this.interProcessCallerCacheBuilders[clazz] = callerBuilder
        }
    }

    override fun <T : Any> getCaller(clazz: Class<T>, address: Address): T? {
        return this.interProcessCallerCacheBuilders[clazz]?.invoke(address) as? T
    }

    override fun <T : Any> putReceiver(clazz: Class<T>, receiver: InvocationReceiver<T>) {
        if (this.interProcessReceiverCache[clazz] == null) {
            this.interProcessReceiverCache[clazz] = receiver
        }
    }

    override fun <T : Any> getReceiver(clazz: Class<T>): InvocationReceiver<T> {
        return requireNotNull(this.interProcessReceiverCache[clazz] as? InvocationReceiver<T>)
    }

    override fun <T : Any> putInstance(clazz: Class<T>, instance: T) {
        this.putReceiver(
            clazz = clazz,
            receiver = object : InvocationReceiver<T> by DefaultInvocationReceiver(instance) {}
        )
    }

}