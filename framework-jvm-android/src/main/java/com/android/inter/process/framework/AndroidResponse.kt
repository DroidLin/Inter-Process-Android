package com.android.inter.process.framework

import android.os.Parcel
import android.os.Parcelable

/**
 * @author: liuzhongao
 * @since: 2024/9/15 11:08
 */
internal data class AndroidResponse(
    val data: Any?,
    val throwable: Throwable?
) : Response, Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readValue(AndroidResponse::class.java.classLoader),
        parcel.readCompatSerializable()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(this.data.typeSafe())
        parcel.writeSerializable(this.throwable)
    }

    override fun describeContents(): Int {
        return if (this.data.containsFileDescriptor()) {
            Parcelable.CONTENTS_FILE_DESCRIPTOR
        } else 0
    }

    companion object CREATOR : Parcelable.Creator<AndroidResponse> {
        override fun createFromParcel(parcel: Parcel): AndroidResponse {
            return AndroidResponse(parcel)
        }

        override fun newArray(size: Int): Array<AndroidResponse?> {
            return arrayOfNulls(size)
        }
    }

}
