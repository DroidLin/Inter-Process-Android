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

    /**
     * time milliseconds in connecting to remote, if remote is not respond,
     * an ConnectTimeoutException is thrown
     *
     * see [com.android.inter.process.framework.exceptions.ConnectTimeoutException] for more information.
     */
    val connectTimeout: Long = 10_000L
)
