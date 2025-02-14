package com.android.inter.process.framework

import android.os.Parcelable
import java.io.Serializable

internal fun checkParameters(request: AndroidRequest) {
    when (request) {
        is AndroidJvmMethodRequest -> {
            val requestParameters = request.methodParameterValues
            requestParameters.forEach { value ->
                require((value is Serializable && value !is Function<*>) || value is Parcelable || value == null || value.javaClass.isPrimitive) {
                    "param: ${value?.javaClass?.name} in ${request.declaredClassFullName}#${request.methodName} should implements java.lang.Serializable or android.os.Parcelable," +
                            "and should not be a raw function type."
                }
            }
        }
    }
}