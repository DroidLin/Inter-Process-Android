package com.android.inter.process.framework.address

import android.content.Context

/**
 * address used to connect to remote process through
 * android-based-component [android.content.ContentProvider]
 *
 * @author: liuzhongao
 * @since: 2024/9/16 11:59
 */
internal data class ContentProviderAndroidAddress(
    override val context: Context,
    override val packageName: String,
    val authorities: String,
) : AndroidAddress
