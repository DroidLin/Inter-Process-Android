package com.android.inter.process.framework

import android.os.Parcel
import android.os.Parcelable

/**
 * @author: liuzhongao
 * @since: 2024/9/15 11:08
 */
internal interface AndroidResponse : Response, Parcelable

internal class NoResponse : AndroidResponse {

    constructor()
    constructor(parcel: Parcel) : this()

    override fun writeToParcel(parcel: Parcel, flags: Int) {
    }

    override val data: Any? get() = null
    override val throwable: Throwable? get() = null

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<NoResponse> {
        override fun createFromParcel(parcel: Parcel): NoResponse {
            return NoResponse(parcel)
        }

        override fun newArray(size: Int): Array<NoResponse?> {
            return arrayOfNulls(size)
        }
    }

}

internal data class DefaultResponse(
    override val data: Any? = null,
    override val throwable: Throwable? = null
) : AndroidResponse {

    constructor(parcel: Parcel) : this(
        parcel.readValue(DefaultResponse::class.java.classLoader),
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

    companion object CREATOR : Parcelable.Creator<DefaultResponse> {
        override fun createFromParcel(parcel: Parcel): DefaultResponse {
            return DefaultResponse(parcel)
        }

        override fun newArray(size: Int): Array<DefaultResponse?> {
            return arrayOfNulls(size)
        }
    }

}
