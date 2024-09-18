package com.android.inter.process.framework.metadata

import com.android.inter.process.framework.Request
import java.io.Serializable
import kotlin.coroutines.Continuation

/**
 * @author liuzhongao
 * @since 2024/9/18 17:04
 */
data class JvmMethodRequest(

    val declaredClassFullName: String,
    /**
     * name of calling method.
     */
    val methodName: String,
    /**
     * full name of java types of function parameters,
     * kotlin suspend function parameter type [Continuation] excluded.
     */
    val methodParameterTypeFullNames: List<String>,
    /**
     * values of function parameter,
     * kotlin suspend function parameter type [Continuation] excluded.
     */
    val methodParameterValues: List<Any?>,
    /**
     * used for performance improvement.
     */
    val uniqueId: String,

    val continuation: Continuation<Any?>? = null
) : Request, Serializable {
    companion object {
        private const val serialVersionUID: Long = 4029814239749099528L
    }
}
