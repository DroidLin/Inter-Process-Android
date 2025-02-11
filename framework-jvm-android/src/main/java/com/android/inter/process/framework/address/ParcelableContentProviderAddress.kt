package com.android.inter.process.framework.address

import android.os.Parcel
import android.os.Parcelable
import com.android.inter.process.framework.address.AndroidAddress.Companion.packageName

/**
 * @author: liuzhongao
 * @since: 2024/9/17 13:40
 */
internal data class ParcelableContentProviderAddress(
    override val packageName: String,
    val authorities: String,
) : ParcelableAndroidAddress {

    constructor(address: ContentProviderAndroidAddress) : this(
        packageName = address.packageName,
        authorities = address.authorities
    )

    constructor(parcel: Parcel) : this(
        requireNotNull(parcel.readString()),
        requireNotNull(parcel.readString()),
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(packageName)
        parcel.writeString(authorities)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ParcelableContentProviderAddress> {
        override fun createFromParcel(parcel: Parcel): ParcelableContentProviderAddress {
            return ParcelableContentProviderAddress(parcel)
        }

        override fun newArray(size: Int): Array<ParcelableContentProviderAddress?> {
            return arrayOfNulls(size)
        }
    }
}