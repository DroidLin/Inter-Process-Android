package com.android.inter.process.test.metadata

import android.os.Parcel
import android.os.Parcelable

data class ParcelableMetadata(
    val number: Int,
    val value: String
) : Parcelable {

    constructor(parcel: Parcel) : this(
        number = parcel.readInt(),
        value = requireNotNull(parcel.readString())
    )

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(number)
        dest.writeString(value)
    }

    companion object {
        @JvmField
        val CREATOR = object  : Parcelable.Creator<ParcelableMetadata> {
            override fun createFromParcel(parcel: Parcel): ParcelableMetadata {
                return ParcelableMetadata(parcel)
            }

            override fun newArray(size: Int): Array<ParcelableMetadata?> {
                return arrayOfNulls(size)
            }
        }

        val Default = ParcelableMetadata(100, "-102")
    }
}