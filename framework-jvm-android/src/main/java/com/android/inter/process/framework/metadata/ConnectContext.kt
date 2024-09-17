package com.android.inter.process.framework.metadata

import android.os.Parcel
import android.os.Parcelable
import com.android.inter.process.framework.address.ParcelableAndroidAddress
import com.android.inter.process.framework.readCompatParcelable

/**
 * @author: liuzhongao
 * @since: 2024/9/16 18:03
 */
internal data class ConnectContext(
    /**
     * indicate the address where the process requires to connect.
     */
    val sourceAddress: ParcelableAndroidAddress,

    /**
     * handle of source process.
     */
    val functionParameter: FunctionParameter,
) : Parcelable {

    constructor(parcel: Parcel) : this(
        requireNotNull(parcel.readCompatParcelable(ConnectContext::class.java.classLoader)),
        requireNotNull(parcel.readCompatParcelable(ConnectContext::class.java.classLoader))
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(this.sourceAddress, 0)
        parcel.writeParcelable(this.functionParameter, 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ConnectContext> {
        override fun createFromParcel(parcel: Parcel): ConnectContext {
            return ConnectContext(parcel)
        }

        override fun newArray(size: Int): Array<ConnectContext?> {
            return arrayOfNulls(size)
        }
    }
}
