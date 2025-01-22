package com.android.inter.process.framework.reflect

import com.android.inter.process.framework.annotation.IPCMethod
import com.android.inter.process.framework.metadata.AndroidBinderFunctionParameter
import com.android.inter.process.framework.metadata.newBinderFunctionParameter
import com.android.inter.process.framework.receiverAndroidFunction

internal abstract class CallerValueTransform<P, T> : ValueTransform<P, T> {

    internal class InterfaceStubToFunctionParameterTransform<T>(
        private val clazz: Class<T>
    ) : CallerValueTransform<T?, AndroidBinderFunctionParameter?>() {
        override fun map(param: T?): AndroidBinderFunctionParameter? {
            return newBinderFunctionParameter(clazz, param as? Any)
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
