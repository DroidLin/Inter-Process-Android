package com.android.inter.process.framework.address

import android.content.Context

/**
 * address used to connect to remote process through
 * android-based-component [android.content.BroadcastReceiver]
 *
 * @author: liuzhongao
 * @since: 2024/9/13 00:24
 */
internal data class BroadcastAndroidAddress(
    override val context: Context,
    override val packageName: String,
    val broadcastAction: String,
) : AndroidAddress