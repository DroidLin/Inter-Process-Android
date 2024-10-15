package com.android.inter.process.framework

import com.android.inter.process.framework.reflect.InvocationReceiver

/**
 * @author: liuzhongao
 * @since: 2024/9/17 16:08
 */
interface ObjectPool {

    fun <T : Any> putCallerBuilder(clazz: Class<T>, callerBuilder: (Address) -> T)

    fun <T : Any> getCaller(clazz: Class<T>, address: Address): T?

    fun <T : Any> putReceiver(clazz: Class<T>, receiver: InvocationReceiver<T>)

    fun <T : Any> getReceiver(clazz: Class<T>): InvocationReceiver<T>

    fun <T : Any> putInstance(clazz: Class<T>, instance: T)
}