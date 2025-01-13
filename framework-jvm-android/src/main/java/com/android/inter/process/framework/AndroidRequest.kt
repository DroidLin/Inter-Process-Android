package com.android.inter.process.framework

import android.os.Parcel
import android.os.Parcelable
import com.android.inter.process.framework.metadata.ConnectContext
import com.android.inter.process.framework.metadata.SuspendContext
import kotlin.coroutines.Continuation

/**
 * for android extensions, all the request are implemented from [AndroidRequest] in order to
 * be passed through binder driver.
 *
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

internal const val TYPE_SET_CONNECT_CONTEXT = "type_set_connect_context"
internal const val TYPE_FETCH_BASIC_CONNECTION_VERSION = "type_fetch_basic_connection_version"
internal const val TYPE_FETCH_REMOTE_CONNECTION_ADDRESS = "type_fetch_remote_connection_address"

/**
 * extensions fulfill better usage of kotlin types instead of String Parameter Access.
 */
internal var BasicConnectionSelfRequest.connectContext: ConnectContext?
    set(value) {
        this.extensions[TYPE_SET_CONNECT_CONTEXT] = value
    }
    get() = this.extensions[TYPE_SET_CONNECT_CONTEXT] as? ConnectContext

internal data class BasicConnectionSelfRequest(
    /**
     * request type of internal basic connection interface.
     */
    val requestType: String,
    /**
     * extra parcelable parameters storage in self request,
     * may be transported through binder driver.
     */
    val extensions: MutableMap<String, Any?> = HashMap()
) : AndroidRequest {

    constructor(parcel: Parcel) : this(
        requestType = requireNotNull(parcel.readString()),
        extensions = parcel.readCompatMap<String, Any>(BasicConnectionSelfRequest::class.java.classLoader)?.toMutableMap() ?: HashMap<String, Any?>()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(requestType)
        parcel.writeMap(this.extensions)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<BasicConnectionSelfRequest> {
        override fun createFromParcel(parcel: Parcel): BasicConnectionSelfRequest {
            return BasicConnectionSelfRequest(parcel)
        }

        override fun newArray(size: Int): Array<BasicConnectionSelfRequest?> {
            return arrayOfNulls(size)
        }
    }
}