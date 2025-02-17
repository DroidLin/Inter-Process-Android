package com.android.inter.process.framework

import com.android.inter.process.framework.metadata.ServiceCreateResource

/**
 * component used to create implementations of interface provided
 * by class in [ServiceCreateResource].
 *
 * @author: liuzhongao
 * @since: 2024/9/10 23:30
 */
interface Component<A : Address> {

    fun <T> serviceCreate(serviceCreateResource: ServiceCreateResource<T, A>): T
}