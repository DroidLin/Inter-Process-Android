package com.android.inter.process.framework.address

import android.os.Parcelable

/**
 * @author: liuzhongao
 * @since: 2024/9/17 13:32
 */
internal interface ParcelableAndroidAddress : Parcelable {

    val packageName: String
}