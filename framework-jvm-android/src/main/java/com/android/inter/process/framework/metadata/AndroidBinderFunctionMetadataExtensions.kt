package com.android.inter.process.framework.metadata

import com.android.inter.process.framework.callerAndroidFunction

fun <T> AndroidBinderFunctionMetadata.function(): T {
    return (this.functionType as Class<T>).callerAndroidFunction(this.androidFunction)
}

/**
 * Returns the last valid index for the array.
 */
public val <T> Array<out T>.lastIndex: Int
    get() = size - 1
