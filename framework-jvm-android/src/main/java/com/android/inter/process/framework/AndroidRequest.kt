package com.android.inter.process.framework

import android.os.Parcel
import android.os.Parcelable
import com.android.inter.process.framework.metadata.SuspendContext
import kotlin.coroutines.Continuation

/**
 * @author: liuzhongao
 * @since: 2024/9/15 10:49
 */
internal data class AndroidRequest(
    val declaredClassFullName: String,
    /**
     * name of calling method.
     */
    val methodName: String,
    /**
     * full name of java types of function parameters,
     * kotlin suspend function parameter type [Continuation] excluded.
     */
    val methodParameterTypeFullNames: List<String>,
    /**
     * values of function parameter,
     * kotlin suspend function parameter type [Continuation] excluded.
     */
    val methodParameterValues: List<Any?>,
    /**
     * used for performance improvement.
     */
    val uniqueId: String,
    /**
     * support kotlin suspend function.
     */
    val suspendContext: SuspendContext? = null,
) : Request, Parcelable {

    constructor(parcel: Parcel) : this(
        requireNotNull(parcel.readString()),
        requireNotNull(parcel.readString()),
        requireNotNull(parcel.readCompatList(AndroidRequest::class.java.classLoader)),
        requireNotNull(parcel.readCompatList(AndroidRequest::class.java.classLoader)),
        requireNotNull(parcel.readString()),
        parcel.readCompatParcelable(AndroidRequest::class.java.classLoader)
    )

    override fun describeContents(): Int {
        return if (this.methodParameterValues.containsFileDescriptor()) {
            Parcelable.CONTENTS_FILE_DESCRIPTOR
        } else 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(this.declaredClassFullName)
        dest.writeString(this.methodName)
        dest.writeList(this.methodParameterTypeFullNames)
        dest.writeList(this.methodParameterValues)
        dest.writeString(this.uniqueId)
        dest.writeParcelable(this.suspendContext, 0)
    }

    companion object CREATOR : Parcelable.Creator<AndroidRequest> {
        override fun createFromParcel(parcel: Parcel): AndroidRequest {
            return AndroidRequest(parcel)
        }

        override fun newArray(size: Int): Array<AndroidRequest?> {
            return arrayOfNulls(size)
        }
    }

}