package com.android.inter.process.framework.reflect

interface ValueTransform<P, T> {

    fun map(param: P): T

    companion object {
        @JvmStatic
        fun nullableTransform(): ValueTransform<Any?, Any?> {
            return NullableValueTransform()
        }
    }
}

private class NullableValueTransform : ValueTransform<Any?, Any?> {
    override fun map(param: Any?): Any? = param
}

