package com.android.inter.process.framework.metadata

import android.os.Parcel
import android.os.Parcelable
import com.android.inter.process.framework.containsFileDescriptor
import com.android.inter.process.framework.readCompatMap

/**
 * transport bridge
 *
 * @author: liuzhongao
 * @since: 2024/9/15 01:51
 */
internal class FunctionParameters private constructor() : Parcelable {

    @Transient
    internal var nextNode: FunctionParameters? = null

    private val innerParameters: MutableMap<String, Any?> = HashMap()

    constructor(parcel: Parcel) : this() {
        this.readFromParcel(parcel)
    }

    /**
     * please make sure everything you put into this map can be
     * serialized in [Parcel]
     */
    operator fun <T : Any> set(key: String, value: T?) {
        this.innerParameters[key] = value
    }

    operator fun <T : Any> get(key: String): T? {
        return this.innerParameters[key] as? T
    }

    public fun readFromParcel(parcel: Parcel) {
        val innerParameterMap = parcel.readCompatMap<String, Any>(this.javaClass.classLoader)
        if (innerParameterMap != null) {
            this.innerParameters.clear()
            this.innerParameters += innerParameterMap
        }
    }

    override fun describeContents(): Int {
        return if (this.innerParameters.containsFileDescriptor()) {
            Parcelable.CONTENTS_FILE_DESCRIPTOR
        } else 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeMap(this.innerParameters)
    }

    fun recycle() {
        this.innerParameters.clear()
        FunctionParameters.recycle(this)
    }

    companion object {

        @JvmField
        val CREATOR = object : Parcelable.Creator<FunctionParameters> {
            override fun createFromParcel(parcel: Parcel): FunctionParameters {
                return FunctionParameters.obtain(parcel)
            }

            override fun newArray(size: Int): Array<FunctionParameters?> {
                return arrayOfNulls(size)
            }
        }

        private var functionParameterHead: FunctionParameters? = null

        @JvmStatic
        @JvmOverloads
        internal fun obtain(parcel: Parcel? = null): FunctionParameters {
            val currentHead = this.functionParameterHead
            if (currentHead != null) {
                synchronized(this) {
                    this.functionParameterHead = currentHead.nextNode
                    currentHead.nextNode = null
                }
                if (parcel != null) {
                    currentHead.readFromParcel(parcel)
                }
                return currentHead
            }
            return if (parcel != null) {
                FunctionParameters(parcel)
            } else FunctionParameters()
        }

        @JvmStatic
        internal fun recycle(functionParameters: FunctionParameters) {
            synchronized(this) {
                functionParameters.nextNode = this.functionParameterHead
                this.functionParameterHead = functionParameters
            }
        }
    }
}