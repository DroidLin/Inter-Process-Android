package com.android.inter.process.framework

/**
 * @author: liuzhongao
 * @since: 2024/9/16 15:13
 */
fun interface IFunction<P : Request, R : Response> {

    fun call(parameter: P): R
}