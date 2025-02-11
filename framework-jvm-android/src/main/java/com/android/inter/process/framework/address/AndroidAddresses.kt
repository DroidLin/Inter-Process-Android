package com.android.inter.process.framework.address

import android.content.Context
import com.android.inter.process.framework.Address

/**
 * @author: liuzhongao
 * @since: 2024/9/17 15:44
 */

fun broadcast(
    context: Context,
    broadcastAction: String,
): AndroidAddress {
    return BroadcastAndroidAddress(
        context = context,
        broadcastAction = broadcastAction
    )
}

fun provider(
    context: Context,
    authorities: String,
): AndroidAddress {
    return ContentProviderAndroidAddress(
        context = context,
        authorities = authorities
    )
}
