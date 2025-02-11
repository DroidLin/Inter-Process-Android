package com.android.inter.process.framework.address

import android.os.Parcel
import android.os.Parcelable
import com.android.inter.process.framework.address.AndroidAddress.Companion.packageName

/**
 * @author: liuzhongao
 * @since: 2024/9/17 13:33
 */
internal data class ParcelableBroadcastAndroidAddress(
    override val packageName: String,
    val broadcastAction: String,
) : ParcelableAndroidAddress {

    constructor(broadcastAndroidAddress: BroadcastAndroidAddress) : this(
        packageName = broadcastAndroidAddress.packageName,
        broadcastAction = broadcastAndroidAddress.broadcastAction
    )

    constructor(parcel: Parcel) : this(
        requireNotNull(parcel.readString()),
        requireNotNull(parcel.readString())
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(packageName)
        parcel.writeString(broadcastAction)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ParcelableBroadcastAndroidAddress> {
        override fun createFromParcel(parcel: Parcel): ParcelableBroadcastAndroidAddress {
            return ParcelableBroadcastAndroidAddress(parcel)
        }

        override fun newArray(size: Int): Array<ParcelableBroadcastAndroidAddress?> {
            return arrayOfNulls(size)
        }
    }
}