package com.android.inter.process.framework.address

import android.content.Context
import com.android.inter.process.framework.Address

/**
 * Represents the Android process name.
 * Each process should be unique and there must be only one.
 *
 * see [BroadcastAddress], [ContentProviderAddress] for more information.
 *
 * @author: liuzhongao
 * @since: 2024/9/13 00:24
 */
sealed interface AndroidAddress : Address {
    /**
     * connect to remote process using android-context.
     */
    val context: Context

    companion object {
        internal fun <T : AndroidAddress> T.toParcelableAddress(): ParcelableAndroidAddress {
            return when (this) {
                is BroadcastAddress -> ParcelableBroadcastAndroidAddress(this)
                is ContentProviderAddress -> ParcelableContentProviderAddress(this)
                else -> throw IllegalArgumentException("unsupported address type: ${this.javaClass}.")
            }
        }

        internal val AndroidAddress.packageName: String
            get() = this.context.packageName
    }
}


