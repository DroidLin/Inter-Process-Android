package com.android.inter.process.framework

import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import java.io.Serializable
import java.util.LinkedList

/**
 * @author: liuzhongao
 * @since: 2024/9/15 10:51
 */

inline fun <reified T : Any> Parcel.readCompatList(classLoader: ClassLoader = requireNotNull(T::class.java.classLoader)): List<T> {
    val mutableList = LinkedList<T>()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        this.readList(mutableList, classLoader, T::class.java)
    } else this.readList(mutableList, classLoader)
    return mutableList
}

inline fun <reified T : Serializable> Parcel.readCompatSerializable(classLoader: ClassLoader = requireNotNull(T::class.java.classLoader)): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        this.readSerializable(classLoader, T::class.java)
    } else this.readSerializable() as? T
}

inline fun <reified T : Parcelable> Parcel.readCompatParcelable(classLoader: ClassLoader = requireNotNull(T::class.java.classLoader)): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        this.readParcelable(classLoader, T::class.java)
    } else this.readParcelable(classLoader)
}
