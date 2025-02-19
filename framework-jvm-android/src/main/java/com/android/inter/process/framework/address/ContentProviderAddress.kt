package com.android.inter.process.framework.address

import android.content.Context
import android.net.Uri
import android.content.ContentProvider

/**
 * address to connect to remote process through [ContentProvider]
 *
 * @author: liuzhongao
 * @since: 2024/9/16 11:59
 */
internal data class ContentProviderAddress(
    override val context: Context,
    val authorities: String,
) : AndroidAddress {

    val uri: Uri by lazy { Uri.Builder().scheme("content").authority(this.authorities).build() }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ContentProviderAddress

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
