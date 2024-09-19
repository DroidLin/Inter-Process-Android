package com.android.inter.process.framework

import android.os.Parcel
import android.os.Parcelable
import com.android.inter.process.framework.metadata.ConnectContext
import com.android.inter.process.framework.metadata.SuspendContext
import kotlin.coroutines.Continuation

/**
 * @author: liuzhongao
 * @since: 2024/9/15 10:49
 */
internal interface AndroidRequest : Request, Parcelable

internal data class AndroidJvmMethodRequest(
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
) : AndroidRequest {

    constructor(parcel: Parcel) : this(
        requireNotNull(parcel.readString()),
        requireNotNull(parcel.readString()),
        requireNotNull(parcel.readCompatList(AndroidJvmMethodRequest::class.java.classLoader)),
        requireNotNull(parcel.readCompatList(AndroidJvmMethodRequest::class.java.classLoader)),
        requireNotNull(parcel.readString()),
        parcel.readCompatParcelable(AndroidJvmMethodRequest::class.java.classLoader)
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

    companion object CREATOR : Parcelable.Creator<AndroidJvmMethodRequest> {
        override fun createFromParcel(parcel: Parcel): AndroidJvmMethodRequest {
            return AndroidJvmMethodRequest(parcel)
        }

        override fun newArray(size: Int): Array<AndroidJvmMethodRequest?> {
            return arrayOfNulls(size)
        }
    }

}

internal data class SetConnectContext(
    val connectContext: ConnectContext
) : AndroidRequest {
    constructor(parcel: Parcel) : this(requireNotNull(parcel.readCompatParcelable(ConnectContext::class.java.classLoader)))

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(this.connectContext, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SetConnectContext> {
        override fun createFromParcel(parcel: Parcel): SetConnectContext {
            return SetConnectContext(parcel)
        }

        override fun newArray(size: Int): Array<SetConnectContext?> {
            return arrayOfNulls(size)
        }
    }
}

internal class FetchBasicConnectionVersion() : AndroidRequest {

    constructor(parcel: Parcel) : this()

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {}

    companion object CREATOR : Parcelable.Creator<FetchBasicConnectionVersion> {
        override fun createFromParcel(parcel: Parcel): FetchBasicConnectionVersion {
            return FetchBasicConnectionVersion(parcel)
        }

        override fun newArray(size: Int): Array<FetchBasicConnectionVersion?> {
            return arrayOfNulls(size)
        }
    }
}