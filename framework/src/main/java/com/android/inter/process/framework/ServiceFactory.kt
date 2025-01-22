package com.android.inter.process.framework

import com.android.inter.process.framework.annotation.IPCService
import com.android.inter.process.framework.annotation.IPCServiceFactory

/**
 * factory to create implementation instance of interface [IPCService]
 * is annotated with.
 *
 * see [IPCServiceFactory] for more information.
 *
 * sample code:
 * ```
 * interface MyInterface
 *
 * class MyInterfaceImpl : MyInterface
 *
 * @IPCServiceFactory(interfaceClazz = MyInterface::class)
 * class MyInterfaceFactory : ServiceFactory<MyInterface> {
 *     override fun serviceCreate(): MyInterface {
 *         // your code
 *         return MyInterfaceImpl()
 *     }
 * }
 * ```
 */
interface ServiceFactory<T> {

    /**
     * called automatically by the ipc framework when it is first accessed.
     */
    fun serviceCreate(): T
}
