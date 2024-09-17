package com.android.inter.process.framework.metadata

import com.android.inter.process.framework.AndroidFunction
import kotlinx.coroutines.Deferred

/**
 * @author: liuzhongao
 * @since: 2024/9/17 03:16
 */
internal data class ConnectRunningTask(
    val deferred: Deferred<AndroidFunction>
)