package com.android.inter.process.framework

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * @author: liuzhongao
 * @since: 2024/9/16 15:34
 */

internal fun <T> SafeContinuation(continuation: Continuation<T>): Continuation<T> {
    return object : Continuation<T> by continuation {
        val continuationConsumed by lazy { AtomicBoolean(false) }
        override fun resumeWith(result: Result<T>) {
            val consumed = this.continuationConsumed.get()
            if (consumed) {
                InterProcessLogger.logDebug("SafeContinuation resume skipped, result: ${result}.")
                return
            }
            if (this.continuationConsumed.compareAndSet(false, true)) {
                continuation.resumeWith(result)
            }
        }
    }
}

internal val ConnectionCoroutineDispatcherScope = CoroutineScope(Dispatchers.IO)

internal fun runOnConnectionScope(
    block: suspend CoroutineScope.() -> Unit
) =
    ConnectionCoroutineDispatcherScope.launch(context = ConnectionCoroutineDispatcherScope.coroutineContext, block = block)

internal suspend fun <T> withConnectionScope(
    block: suspend CoroutineScope.() -> T
): T {
    return withContext(
        context = ConnectionCoroutineDispatcherScope.coroutineContext,
        block = block
    )
}

internal fun <T> blockingOnConnectionScope(
    block: suspend CoroutineScope.() -> T
) = runBlocking(ConnectionCoroutineDispatcherScope.coroutineContext, block)