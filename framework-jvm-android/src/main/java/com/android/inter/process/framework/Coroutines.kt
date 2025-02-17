package com.android.inter.process.framework

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

internal fun <T> SafeContinuation(continuation: Continuation<T>): Continuation<T> {
    return object : Continuation<T> by continuation {

        private val continuationResumed by lazy { AtomicBoolean(false) }

        override fun resumeWith(result: Result<T>) {
            val consumed = this.continuationResumed.get()
            if (consumed) {
                if (isDebugMode) {
                    Logger.logDebug("SafeContinuation resume skipped, result: ${result}.")
                }
                return
            }
            if (this.continuationResumed.compareAndSet(false, true)) {
                continuation.resumeWith(result)
            }
        }
    }
}

internal val ConnectionCoroutineDispatcherScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

internal fun runOnConnectionScope(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    block: suspend CoroutineScope.() -> Unit
) =
    ConnectionCoroutineDispatcherScope.launch(context = ConnectionCoroutineDispatcherScope.coroutineContext + coroutineContext, block = block)

internal suspend fun <T> withConnectionScope(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    block: suspend CoroutineScope.() -> T
): T = withContext(
    context = ConnectionCoroutineDispatcherScope.coroutineContext + coroutineContext,
    block = block
)

internal fun <T> blockingOnConnectionScope(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    block: suspend CoroutineScope.() -> T
) = runBlocking(ConnectionCoroutineDispatcherScope.coroutineContext + coroutineContext, block)