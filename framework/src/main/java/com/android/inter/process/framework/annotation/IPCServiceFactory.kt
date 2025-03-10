package com.android.inter.process.framework.annotation

import kotlin.reflect.KClass
import com.android.inter.process.framework.ServiceFactory

/**
 * collect implementations of interfaces annotated with [IPCService].
 *
 * see [ServiceFactory] for more information.
 *
 * **Note**: the factory must only have one default constructor, and the number
 * of constructor parameters should be 0.
 *
 * @param interfaceClazz implementation of this factory is generated to.
 * @param uniqueKey unique key for this implementation.
 */
@Retention(value = AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class IPCServiceFactory(val interfaceClazz: KClass<*>, val uniqueKey: String = "")