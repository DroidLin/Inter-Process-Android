package com.android.inter.process.framework.address

import android.content.Context
import com.android.inter.process.framework.Address

/**
 * @author: liuzhongao
 * @since: 2024/9/17 15:44
 */

@JvmOverloads
fun broadcast(
    context: Context,
    broadcastAction: String,
    packageName: String? = null
): AndroidAddress {
    return BroadcastAndroidAddress(
        context = context,
        packageName = packageName ?: context.packageName,
        broadcastAction = broadcastAction
    )
}

@JvmOverloads
fun provider(
    context: Context,
    packageName: String? = null,
    authorities: String,
): AndroidAddress {
    return ContentProviderAndroidAddress(
        context = context,
        packageName = packageName ?: context.packageName,
        authorities = authorities
    )
}
