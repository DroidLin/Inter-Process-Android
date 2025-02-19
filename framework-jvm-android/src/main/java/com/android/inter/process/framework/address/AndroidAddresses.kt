package com.android.inter.process.framework.address

import android.content.Context

/**
 * @author: liuzhongao
 * @since: 2024/9/17 15:44
 */

fun broadcast(
    context: Context,
    broadcastAction: String,
): AndroidAddress {
    return BroadcastAddress(
        context = context,
        broadcastAction = broadcastAction
    )
}

fun provider(
    context: Context,
    authorities: String,
): AndroidAddress {
    return ContentProviderAddress(
        context = context,
        authorities = authorities
    )
}
