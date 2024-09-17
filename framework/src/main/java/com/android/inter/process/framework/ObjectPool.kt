package com.android.inter.process.framework

import com.android.inter.process.framework.reflect.InvocationReceiver

/**
 * @author: liuzhongao
 * @since: 2024/9/17 16:08
 */
interface ObjectPool {

    fun <T : Any> putCallerFactory(clazz: Class<T>, factory: (Address) -> T)

    fun <T : Any> getCaller(clazz: Class<T>, address: Address): T?

    fun <T : Any> putReceiver(clazz: Class<T>, impl: InvocationReceiver<T>)

    fun <T : Any> putReceiverFactory(clazz: Class<T>, factory: () -> InvocationReceiver<T>)

    fun <T : Any> putInstance(clazz: Class<T>, instance: T)

    fun <T : Any> getInstance(clazz: Class<T>): T
}