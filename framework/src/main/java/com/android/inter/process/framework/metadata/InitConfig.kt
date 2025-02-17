package com.android.inter.process.framework.metadata

import com.android.inter.process.framework.Address

open class InitConfig(
    /**
     * init default address of current process, which will be transported to remote,
     * ensure this method be called once during the application
     * lifecycle to avoid potential issues.
     */
    val initAddress: Address,

    /**
     * controls debug logs print during runtime.
     */
    val debugMode: Boolean = false,
)
