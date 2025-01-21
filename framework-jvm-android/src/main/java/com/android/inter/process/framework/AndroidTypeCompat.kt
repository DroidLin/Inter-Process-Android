package com.android.inter.process.framework

import android.os.Parcel
import android.os.Parcelable
import android.util.ArrayMap
import android.util.SparseArray
import java.io.FileDescriptor

fun Any?.containsFileDescriptor(): Boolean {
    if (this == null) return false
    if (this is FileDescriptor) return true
    return when (this) {
        is Parcel -> this.hasFileDescriptors()
        is Parcelable -> this.describeContents() == Parcelable.CONTENTS_FILE_DESCRIPTOR
        is ArrayMap<*, *> -> this.any { (key, value) -> (key.containsFileDescriptor() || value.containsFileDescriptor()) }
        is Map<*, *> -> this.any { (key, value) -> (key.containsFileDescriptor() || value.containsFileDescriptor()) }
        is SparseArray<*> -> {
            var hasFileDescriptor = false
            for (index in 0 until this.size()) {
                hasFileDescriptor = this[index].containsFileDescriptor()
                if (hasFileDescriptor) break
            }
            hasFileDescriptor
        }
        is Array<*> -> this.any { it.containsFileDescriptor() }
        is Iterable<*> -> this.any { it.containsFileDescriptor() }
        else -> false
    }
}
