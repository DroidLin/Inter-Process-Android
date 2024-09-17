package com.android.inter.process.framework

import com.android.inter.process.framework.reflect.InvocationReceiver
import java.lang.reflect.InvocationHandler

/**
 * @author: liuzhongao
 * @since: 2024/9/8 23:34
 */
val objectPool: ObjectPool by lazy { object : ObjectPool by InterProcessObjectPool {} }

internal object InterProcessObjectPool : ObjectPool {

    private val invocationHandlerCache: MutableMap<Class<*>, InvocationHandler> by lazy { HashMap() }

    private val interProcessCallerFactoryCache: MutableMap<Class<*>, (Address) -> Any> by lazy { HashMap() }
    private val interProcessReceiverCache: MutableMap<Class<*>, InvocationReceiver<*>> by lazy { HashMap() }
    private val interProcessReceiverFactoryCache: MutableMap<Class<*>, () -> InvocationReceiver<*>> by lazy { HashMap() }

    private val interProcessInstance: MutableMap<Class<*>, Any> by lazy { HashMap() }

    fun tryGetInvocationHandler(
        clazz: Class<*>,
        factory: () -> InvocationHandler
    ): InvocationHandler {
        if (this.invocationHandlerCache[clazz] != null) {
            return requireNotNull(this.invocationHandlerCache[clazz])
        }
        if (this.invocationHandlerCache[clazz] == null) {
            synchronized(this.invocationHandlerCache) {
                if (this.invocationHandlerCache[clazz] == null) {
                    this.invocationHandlerCache[clazz] = factory()
                }
            }
        }
        return requireNotNull(this.invocationHandlerCache[clazz])
    }

    fun <T : Any> putCallerFactoryCache(clazz: Class<T>, callerFactory: (Address) -> T) {
        if (this.interProcessCallerFactoryCache[clazz] == null) {
            synchronized(this.interProcessCallerFactoryCache) {
                if (this.interProcessCallerFactoryCache[clazz] == null) {
                    this.interProcessCallerFactoryCache[clazz] = callerFactory
                }
            }
        }
    }

    fun <T : Any> getCallerCache(clazz: Class<T>, address: Address): T? {
        return synchronized(this.interProcessCallerFactoryCache) {
            this.interProcessCallerFactoryCache[clazz]?.invoke(address)
        } as? T
    }

    fun <T : Any> putReceiverCache(clazz: Class<T>, receiver: InvocationReceiver<T>) {
        if (this.interProcessReceiverCache[clazz] == null) {
            synchronized(this.interProcessReceiverCache) {
                if (this.interProcessReceiverCache[clazz] == null) {
                    this.interProcessReceiverCache[clazz] = receiver
                }
            }
        }
    }

    fun <T : Any> putReceiverFactoryCache(
        clazz: Class<T>,
        receiverFactory: () -> InvocationReceiver<T>
    ) {
        if (this.interProcessReceiverFactoryCache[clazz] == null) {
            synchronized(this.interProcessReceiverFactoryCache) {
                if (this.interProcessReceiverFactoryCache[clazz] == null) {
                    this.interProcessReceiverFactoryCache[clazz] = receiverFactory
                }
            }
        }
    }

    /**
     * need performance implemented
     */
    fun <T : Any> getReceiverCache(clazz: Class<T>): InvocationReceiver<T>? {
        if (this.interProcessReceiverCache[clazz] != null) {
            return requireNotNull(this.interProcessReceiverCache[clazz] as? InvocationReceiver<T>)
        }

        if (this.interProcessReceiverCache[clazz] == null) {
            synchronized(this.interProcessReceiverCache) {
                if (this.interProcessReceiverCache[clazz] == null) {
                    val factory = this.interProcessReceiverFactoryCache[clazz]
                    if (factory != null) {
                        val receiver = factory()
                        this.interProcessReceiverCache[clazz] = receiver
                    }
                }
            }
        }
        return this.interProcessReceiverCache[clazz] as? InvocationReceiver<T>
    }

    fun <T : Any> putInterfaceImplementation(clazz: Class<T>, impl: T) {
        synchronized(this.interProcessInstance) {
            this.interProcessInstance[clazz] = impl
        }
    }

    fun <T : Any> getInterfaceImpl(clazz: Class<T>): T? {
        return synchronized(this.interProcessInstance) {
            this.interProcessInstance[clazz] as? T
        }
    }

    override fun <T : Any> putCallerFactory(clazz: Class<T>, factory: (Address) -> T) {
        this.putCallerFactoryCache(clazz, factory)
    }

    override fun <T : Any> getCaller(clazz: Class<T>, address: Address): T? {
        return this.getCallerCache(clazz, address)
    }

    override fun <T : Any> putReceiver(clazz: Class<T>, impl: InvocationReceiver<T>) {
        this.putReceiverCache(clazz, impl)
    }

    override fun <T : Any> putReceiverFactory(
        clazz: Class<T>,
        factory: () -> InvocationReceiver<T>
    ) {
        this.putReceiverFactoryCache(clazz, factory)
    }

    override fun <T : Any> putInstance(clazz: Class<T>, instance: T) {
        this.putInterfaceImplementation(clazz, instance)
    }

    override fun <T : Any> getInstance(clazz: Class<T>): T {
        return requireNotNull(this.getInterfaceImpl(clazz)) {
            "there is no instance of interface: ${clazz}, have you ever put it in?"
        }
    }
}