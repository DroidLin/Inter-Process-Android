package com.android.inter.process.framework

import android.os.Binder
import android.os.IBinder
import android.os.IInterface
import android.os.Parcel
import android.os.Parcelable
import com.android.inter.process.framework.exceptions.ParcelableToLargeException
import com.android.inter.process.framework.metadata.FunctionParameters

/**
 * @author liuzhongao
 * @since 2025/1/21 14:41
 */
internal interface AIDLFunction : IInterface {

    fun call(functionParameters: FunctionParameters)

    class Default : AIDLFunction {
        override fun call(functionParameters: FunctionParameters) {}
        override fun asBinder(): IBinder? = null
    }

    abstract class Stub : Binder(), AIDLFunction {

        init {
            this.attachInterface(this, DESCRIPTOR)
        }

        override fun asBinder(): IBinder = this

        override fun onTransact(code: Int, data: Parcel, reply: Parcel?, flags: Int): Boolean {
            checkNotNull(reply)
            val descriptor = DESCRIPTOR
            if (code >= IBinder.FIRST_CALL_TRANSACTION && code <= IBinder.LAST_CALL_TRANSACTION) {
                data.enforceInterface(descriptor)
            }
            when (code) {
                INTERFACE_TRANSACTION -> {
                    reply.writeString(descriptor)
                }

                TRANSACTION_CALL -> {
                    val functionParameters = readTypedObject(data, FunctionParameters.CREATOR)
                    checkNotNull(functionParameters) { "function parameters is null." }
                    this.call(functionParameters)
                    reply.writeNoException()
                    writeTypedObject(
                        reply,
                        functionParameters,
                        Parcelable.PARCELABLE_WRITE_RETURN_VALUE
                    )
                    if (reply.dataSize() > PARCEL_MAX_COUNT_THRESHOLD) {
                        throw ParcelableToLargeException("return data count: ${reply.dataSize()} reaches max limit.")
                    }
                }

                else -> return super.onTransact(code, data, reply, flags)
            }
            return true
        }

        private class Proxy(private val remote: IBinder) : AIDLFunction {

            val interfaceDescriptor: String get() = DESCRIPTOR

            override fun asBinder(): IBinder = this.remote

            override fun call(functionParameters: FunctionParameters) {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInterfaceToken(this.interfaceDescriptor)
                    writeTypedObject(data, functionParameters, 0)
                    val dataSize = data.dataSize()
                    if (dataSize > PARCEL_MAX_COUNT_THRESHOLD) {
                        throw ParcelableToLargeException("data count: ${dataSize} reaches max limit.")
                    }
                    // real call to remote.
                    val status = this.remote.transact(TRANSACTION_CALL, data, reply, 0)
                    reply.readException()
                    if (reply.readInt() != 0) {
                        functionParameters.readFromParcel(reply)
                    }
                } finally {
                    data.recycle()
                    reply.recycle()
                }
            }
        }

        companion object {

            private const val TRANSACTION_CALL = IBinder.FIRST_CALL_TRANSACTION

            @JvmStatic
            fun asInterface(binderObj: IBinder?): AIDLFunction? {
                binderObj ?: return null
                val iin = binderObj.queryLocalInterface(DESCRIPTOR)
                if (iin != null && iin is AIDLFunction) {
                    return iin
                }
                return Proxy(binderObj)
            }
        }
    }

    companion object {
        private const val DESCRIPTOR = "com.android.inter.process.framework.AIDLFunction"
        private const val PARCEL_MAX_COUNT_THRESHOLD = 2 * (1 shl 10 shl 10)
    }
}

private fun <T : Any> readTypedObject(parcel: Parcel, creator: Parcelable.Creator<T>): T? {
    return if (parcel.readInt() != 0) {
        creator.createFromParcel(parcel)
    } else null
}

private fun <T : Parcelable> writeTypedObject(parcel: Parcel, value: T?, parcelableFlags: Int) {
    if (value != null) {
        parcel.writeInt(1)
        value.writeToParcel(parcel, parcelableFlags)
    } else {
        parcel.writeInt(0)
    }
}