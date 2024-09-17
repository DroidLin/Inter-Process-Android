package com.android.inter.process.framework.metadata

import android.os.Parcel
import android.os.Parcelable
import com.android.inter.process.framework.readCompatParcelable
import com.android.inter.process.framework.readCompatSerializable

/**
 * @author: liuzhongao
 * @since: 2024/9/16 16:13
 */
internal data class SuspendContext(
    val coroutineDispatcher: CoroutineDispatcher = CoroutineDispatcher.None,
    val functionParameter: FunctionParameter
) : Parcelable {

    constructor(parcel: Parcel) : this(
        requireNotNull(parcel.readCompatSerializable()),
        requireNotNull(parcel.readCompatParcelable(SuspendContext::class.java.classLoader))
    )

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeSerializable(this.coroutineDispatcher)
        dest.writeParcelable(this.functionParameter, 0)
    }

    companion object CREATOR : Parcelable.Creator<SuspendContext> {
        override fun createFromParcel(parcel: Parcel): SuspendContext {
            return SuspendContext(parcel)
        }

        override fun newArray(size: Int): Array<SuspendContext?> {
            return arrayOfNulls(size)
        }
    }
}
