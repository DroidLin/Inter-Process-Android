package com.android.inter.process.framework.reflect

import com.android.inter.process.framework.annotation.IPCMethod
import com.android.inter.process.framework.metadata.FunctionParameter
import com.android.inter.process.framework.receiverAndroidFunction

internal abstract class CallerValueTransform<P, T> : ValueTransform<P, T> {

    internal class InterfaceStubToFunctionParameterTransform<T>(private val clazz: Class<T>) :
        CallerValueTransform<T, FunctionParameter>() {
        override fun map(param: T): FunctionParameter {
            return FunctionParameter(
                functionType = clazz,
                androidFunction = clazz.receiverAndroidFunction(param)
            )
        }
    }

    companion object {
        @JvmStatic
        fun <P, T> parseAnnotation(annotation: Annotation, clazz: Class<P>): ValueTransform<P, T>? {
            return when (annotation) {
                is IPCMethod -> InterfaceStubToFunctionParameterTransform(clazz) as ValueTransform<P, T>
                else -> null
            }
        }
    }
}
