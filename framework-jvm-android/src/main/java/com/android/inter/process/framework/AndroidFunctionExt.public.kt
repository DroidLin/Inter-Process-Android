package com.android.inter.process.framework

import android.os.IBinder

fun <T> Class<T>.delegateWithBinder(impl: T): IBinder {
    require(this.isInstance(impl))
    return this.receiverAndroidFunction(impl).function.asBinder()
}

fun <T> Class<T>.delegatorInBinder(iBinder: IBinder): T {
    return this.callerAndroidFunction(AndroidFunctionProxy(requireNotNull(AIDLFunction.Stub.asInterface(iBinder))))
}