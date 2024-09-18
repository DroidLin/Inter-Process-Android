package com.android.inter.process.framework

/**
 * @author: liuzhongao
 * @since: 2024/9/8 18:28
 */
interface Response {

    val data: Any?

    val throwable: Throwable?
}