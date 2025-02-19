package com.android.inter.process.framework.address

import android.content.Context
import android.content.BroadcastReceiver

/**
 * address to connect to remote process through [BroadcastReceiver]
 *
 * @author: liuzhongao
 * @since: 2024/9/13 00:24
 */
internal data class BroadcastAddress(
    override val context: Context,
    val broadcastAction: String,
) : AndroidAddress {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BroadcastAddress

        if (context.packageName != other.context.packageName) return false
        if (broadcastAction != other.broadcastAction) return false

        return true
    }

    override fun hashCode(): Int {
        var result = context.packageName.hashCode()
        result = 31 * result + broadcastAction.hashCode()
        return result
    }
}