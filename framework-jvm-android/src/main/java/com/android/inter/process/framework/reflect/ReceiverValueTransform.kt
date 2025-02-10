package com.android.inter.process.framework.reflect

import com.android.inter.process.framework.annotation.IPCFunction
import com.android.inter.process.framework.metadata.AndroidBinderFunctionMetadata
import com.android.inter.process.framework.metadata.function

internal sealed class ReceiverValueTransform<P, T> : ValueTransform<P, T> {

    private class FunctionParameterToProxyInstanceTransform<T> : ReceiverValueTransform<AndroidBinderFunctionMetadata?, T?>() {
        override fun map(param: AndroidBinderFunctionMetadata?): T? {
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
