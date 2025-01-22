package com.android.inter.process.framework.metadata

import android.os.Parcel
import android.os.Parcelable
import com.android.inter.process.framework.AndroidFunction
import com.android.inter.process.framework.AndroidFunctionProxy
import com.android.inter.process.framework.AIDLFunction
import com.android.inter.process.framework.function
import com.android.inter.process.framework.receiverAndroidFunction
import com.android.inter.process.framework.stringType2ClassType

/**
 * a record that represent a function need to be called through two processes.
 *
 * @author: liuzhongao
 * @since: 2024/9/17 23:11
 */
data class AndroidBinderFunctionParameter internal constructor(
    val functionType: Class<*>,
    internal val androidFunction: AndroidFunction
) : Parcelable {

    constructor(parcel: Parcel) : this(
        requireNotNull(parcel.readString()).stringType2ClassType,
        AndroidFunctionProxy(requireNotNull(AIDLFunction.Stub.asInterface(parcel.readStrongBinder())))
    )

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(this.functionType.name)
        dest.writeStrongBinder(this.androidFunction.function.asBinder())
    }

    companion object CREATOR : Parcelable.Creator<AndroidBinderFunctionParameter> {
        override fun createFromParcel(parcel: Parcel): AndroidBinderFunctionParameter {
            return AndroidBinderFunctionParameter(parcel)
        }

        override fun newArray(size: Int): Array<AndroidBinderFunctionParameter?> {
            return arrayOfNulls(size)
        }
    }
}

fun newBinderFunctionParameter(clazz: Class<*>, instance: Any?): AndroidBinderFunctionParameter? {
    if (instance == null) return null
    if (!clazz.isInstance(instance)) error("${instance.javaClass} is not the instance of ${clazz.name}.")
    return AndroidBinderFunctionParameter(clazz, (clazz as Class<Any>).receiverAndroidFunction(instance))
}