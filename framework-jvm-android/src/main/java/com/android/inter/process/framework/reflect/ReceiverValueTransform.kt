package com.android.inter.process.framework.reflect

import com.android.inter.process.framework.annotation.IPCFunction
import com.android.inter.process.framework.metadata.AndroidBinderFunctionParameter
import com.android.inter.process.framework.metadata.function

internal abstract class ReceiverValueTransform<P, T> : ValueTransform<P, T> {

    private class FunctionParameterToProxyInstanceTransform<T> : ReceiverValueTransform<AndroidBinderFunctionParameter?, T?>() {
        override fun map(param: AndroidBinderFunctionParameter?): T? {
            return param?.function<T>()
        }
    }

    companion object {
        @JvmStatic
        fun <P, T> parseAndroidValueTransform(annotation: Annotation): ValueTransform<P, T>? {
            return when (annotation) {
                is IPCFunction -> FunctionParameterToProxyInstanceTransform<T>() as ValueTransform<P, T>
                else -> null
            }
        }
    }
}
