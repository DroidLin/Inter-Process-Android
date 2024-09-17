package com.android.inter.process.framework.metadata

import com.android.inter.process.framework.Address

/**
 * a struct to store about which interface you want to generate,
 * which location you want to connect, and some extension parameters.
 *
 * @author: liuzhongao
 * @since: 2024/9/11 22:12
 */
data class ServiceCreateResource<T : Any, A : Address>(
    /**
     * target class of an interface.
     */
    val clazz: Class<T>,

    /**
     * address for inter process call, may be implemented as different data type.
     */
    val interProcessAddress: A,
)
