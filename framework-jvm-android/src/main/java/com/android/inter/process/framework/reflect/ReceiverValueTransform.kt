package com.android.inter.process.framework.reflect

import com.android.inter.process.framework.annotation.IPCMethod
import com.android.inter.process.framework.metadata.FunctionParameter
import com.android.inter.process.framework.metadata.function

internal abstract class ReceiverValueTransform<P, T> : ValueTransform<P, T> {

    private class FunctionParameterToProxyInstanceTransform<T> : ReceiverValueTransform<FunctionParameter, T>() {
        override fun map(param: FunctionParameter): T {
            return param.function<T>()
        }
    }

    companion object {
        @JvmStatic
        fun <P, T> parseAndroidValueTransform(annotation: Annotation): ValueTransform<P, T>? {
            return when (annotation) {
                is IPCMethod -> FunctionParameterToProxyInstanceTransform<T>() as ValueTransform<P, T>
                else -> null
            }
        }
    }
}
