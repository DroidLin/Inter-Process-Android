package com.android.inter.process.framework.reflect

import java.lang.reflect.Method
import java.util.LinkedList

internal abstract class AndroidServiceMethod : ServiceMethod {

    internal abstract class AndroidCallerServiceMethod : AndroidServiceMethod() {

        abstract operator fun invoke(parameters: Array<Any?>): Array<Any?>
    }

    internal abstract class AndroidReceiverServiceMethod : AndroidServiceMethod() {

        abstract operator fun invoke(obj: Any, vararg params: Any?): Any?
    }

    private class ReflectCallerServiceMethod(
        val method: Method,
        val parameterValueTransforms: List<ValueTransform<Any?, Any?>>
    ) : AndroidCallerServiceMethod() {
        override operator fun invoke(parameters: Array<Any?>): Array<Any?> {
            check(parameters.size == parameterValueTransforms.size) {
                "input parameters size invalid, requireSize: ${parameterValueTransforms.size}, now: ${parameters.size}."
            }
            for (index in parameters.indices) {
                val valueTransform = this.parameterValueTransforms[index]
                val sourceValue = parameters[index]
                val transformedValue = valueTransform.map(sourceValue)
                parameters[index] = transformedValue
            }
            return parameters
        }
    }

    private class ReflectReceiverServiceMethod(
        val method: Method,
        val parameterValueTransforms: List<ValueTransform<Any?, Any?>>
    ) : AndroidReceiverServiceMethod() {

        override operator fun invoke(obj: Any, vararg params: Any?): Any? {
            check(params.size == parameterValueTransforms.size) {
                "input parameters size invalid, requireSize: ${parameterValueTransforms.size}, now: ${params.size}."
            }
            for (index in params.indices) {
                val valueTransform = this.parameterValueTransforms[index]
                val sourceValue = params[index]
                val transformedValue = valueTransform.map(sourceValue)
                (params as Array<Any?>)[index] = transformedValue
            }
            return method.invoke(obj, *params)
        }
    }

    companion object {

        /**
         * check annotations on function parameters and generate specific transformer
         * for target parameters.
         */
        @JvmStatic
        fun parseCallerServiceMethod(method: Method): AndroidCallerServiceMethod {
            val parameterTypes = method.parameterTypes
            val parameterAnnotations = method.parameterAnnotations
            val parameterValueTransforms = LinkedList<ValueTransform<Any?, Any?>>()
            for (index in parameterAnnotations.indices) {
                var valueParameterAnnotationTransform: ValueTransform<*, *>? = null
                for (parameterAnnotationIndex in parameterAnnotations[index].indices) {
                    val annotation = parameterAnnotations[index][parameterAnnotationIndex]
                    val parameterClass = parameterTypes[index]
                    val valueTransform = CallerValueTransform.parseAnnotation<Any?, Any?>(annotation, parameterClass as Class<Any?>) ?: continue
                    if (valueParameterAnnotationTransform != null) {
                        error("multiple ipc annotations found, only one allowed.")
                    }
                    valueParameterAnnotationTransform = valueTransform
                }
                parameterValueTransforms += ((valueParameterAnnotationTransform ?: ValueTransform.nullableTransform()) as ValueTransform<Any?, Any?>)
            }
            return ReflectCallerServiceMethod(method, parameterValueTransforms)
        }

        @JvmStatic
        fun parseReceiverServiceMethod(method: Method): AndroidReceiverServiceMethod {
            val parameterAnnotations = method.parameterAnnotations
            val parameterValueTransforms = LinkedList<ValueTransform<Any?, Any?>>()
            for (index in parameterAnnotations.indices) {
                var valueParameterAnnotationTransform: ValueTransform<*, *>? = null
                for (parameterAnnotationIndex in parameterAnnotations[index].indices) {
                    val annotation = parameterAnnotations[index][parameterAnnotationIndex]
                    val valueTransform = ReceiverValueTransform.parseAndroidValueTransform<Any?, Any?>(annotation) ?: continue
                    if (valueParameterAnnotationTransform != null) {
                        error("multiple ipc annotations found, only one allowed.")
                    }
                    valueParameterAnnotationTransform = valueTransform
                }
                parameterValueTransforms += ((valueParameterAnnotationTransform ?: ValueTransform.nullableTransform()) as ValueTransform<Any?, Any?>)
            }
            return ReflectReceiverServiceMethod(method, parameterValueTransforms)
        }
    }
}