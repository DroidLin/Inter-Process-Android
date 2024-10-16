package com.android.inter.process.framework.metadata

import android.os.Parcel
import android.os.Parcelable
import com.android.inter.process.framework.AndroidFunction
import com.android.inter.process.framework.Function
import com.android.inter.process.framework.function
import com.android.inter.process.framework.stringType2ClassType

/**
 * a record that represent a function need to be called through two processes.
 *
 * @author: liuzhongao
 * @since: 2024/9/17 23:11
 */
internal data class FunctionParameter(
    val functionType: Class<*>,
    val androidFunction: AndroidFunction
) : Parcelable {

    constructor(parcel: Parcel) : this(
        requireNotNull(parcel.readString()).stringType2ClassType,
        AndroidFunction(Function.Stub.asInterface(parcel.readStrongBinder()))
    )

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(this.functionType.name)
        dest.writeStrongBinder(this.androidFunction.function.asBinder())
    }

    companion object CREATOR : Parcelable.Creator<FunctionParameter> {
        override fun createFromParcel(parcel: Parcel): FunctionParameter {
            return FunctionParameter(parcel)
        }

        override fun newArray(size: Int): Array<FunctionParameter?> {
            return arrayOfNulls(size)
        }
    }

}