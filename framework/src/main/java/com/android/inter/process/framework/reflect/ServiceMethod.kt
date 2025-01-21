package com.android.inter.process.framework.reflect

import java.lang.reflect.Method

/**
 *
 */
interface ServiceMethod {

    companion object {

        @JvmStatic
        fun parseAnnotation(method: Method): ServiceMethod {
            TODO()
        }
    }
}