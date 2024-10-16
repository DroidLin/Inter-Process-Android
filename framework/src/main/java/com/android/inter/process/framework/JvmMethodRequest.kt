package com.android.inter.process.framework

import java.io.Serializable

/**
 * instantiate in code generate by ksp compiler or kapt compiler,
 *
 * @author: liuzhongao
 * @since: 2024/10/17 00:54
 */
data class JvmMethodRequest(
    /**
     * indicate where this method is called from.
     */
    val clazz: Class<*>,
    /**
     * special identifier in [clazz] and is unique in this class declaration.
     */
    val functionIdentifier: String,
    /**
     * function parameters in function call.
     */
    val functionParameters: List<Any?>,
    /**
     * suspend flag.
     */
    val isSuspend: Boolean
) : Request, Serializable {
    companion object {
        private const val serialVersionUID: Long = -5077850849886473899L
    }
}