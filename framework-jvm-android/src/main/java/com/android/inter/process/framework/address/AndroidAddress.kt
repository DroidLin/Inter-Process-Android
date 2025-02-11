package com.android.inter.process.framework.address

import android.content.Context
import com.android.inter.process.framework.Address

/**
 * address represent
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
                is BroadcastAndroidAddress -> ParcelableBroadcastAndroidAddress(this)
                is ContentProviderAndroidAddress -> ParcelableContentProviderAddress(this)
                else -> throw IllegalArgumentException("unsupported address type: ${this.javaClass}.")
            }
        }

        internal val AndroidAddress.packageName: String
            get() = this.context.packageName
    }
}


