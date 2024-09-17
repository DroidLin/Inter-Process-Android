package com.android.inter.process.framework

import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext

/**
 * @author: liuzhongao
 * @since: 2024/9/16 14:10
 */
interface IContinuation<in T> : Continuation<T>

fun <T> IContinuation(continuation: Continuation<T>): IContinuation<T> {
    return object : IContinuation<T> {
        override val context: CoroutineContext
            get() = continuation.context

        override fun resumeWith(result: Result<T>) {
            continuation.resumeWith(result)
        }
    }
}