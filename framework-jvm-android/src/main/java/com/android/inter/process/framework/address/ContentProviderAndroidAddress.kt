package com.android.inter.process.framework.address

import android.content.Context
import android.net.Uri

/**
 * address used to connect to remote process through
 * android-based-component [android.content.ContentProvider]
 *
 * @author: liuzhongao
 * @since: 2024/9/16 11:59
 */
internal data class ContentProviderAndroidAddress(
    override val context: Context,
    val authorities: String,
) : AndroidAddress {

    val uri: Uri by lazy { Uri.Builder().scheme("content").authority(this.authorities).build() }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ContentProviderAndroidAddress

        if (context.packageName != other.context.packageName) return false
        if (authorities != other.authorities) return false

        return true
    }

    override fun hashCode(): Int {
        var result = context.packageName.hashCode()
        result = 31 * result + authorities.hashCode()
        return result
    }
}
