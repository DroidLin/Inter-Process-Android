package com.android.inter.process.framework.metadata

import android.os.Parcel
import android.os.Parcelable
import com.android.inter.process.framework.AndroidFunctionProxy
import com.android.inter.process.framework.BasicConnection
import com.android.inter.process.framework.BasicConnectionProxy
import com.android.inter.process.framework.AIDLFunction
import com.android.inter.process.framework.address.ParcelableAndroidAddress
import com.android.inter.process.framework.iBinder
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

    val basicConnection: BasicConnection,
) : Parcelable {

    constructor(parcel: Parcel) : this(
        requireNotNull(parcel.readCompatParcelable(ConnectContext::class.java.classLoader)),
        BasicConnectionProxy(AndroidFunctionProxy(requireNotNull(AIDLFunction.Stub.asInterface(parcel.readStrongBinder()))))
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(this.sourceAddress, 0)
        parcel.writeStrongBinder(this.basicConnection.iBinder)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object {

        @JvmField
        val CREATOR = object : Parcelable.Creator<ConnectContext> {
            override fun createFromParcel(parcel: Parcel): ConnectContext {
                return ConnectContext(parcel)
            }

            override fun newArray(size: Int): Array<ConnectContext?> {
                return arrayOfNulls(size)
            }
        }
    }

}
