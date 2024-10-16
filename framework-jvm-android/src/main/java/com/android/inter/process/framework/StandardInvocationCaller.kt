package com.android.inter.process.framework

import com.android.inter.process.framework.reflect.InvocationCaller
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.Continuation

/**
 * @author: liuzhongao
 * @since: 2024/9/16 15:05
 */
internal class StandardInvocationCaller(
    private val connection: Connection,
) : InvocationCaller {
    override fun invoke(request: JvmReflectMethodRequest): Any? {
        return if (request.continuation != null) {
            (this.connection::call as Function2<Request, Continuation<Any?>, Any?>).invoke(request, requireNotNull(request.continuation))
        } else runBlocking { this@StandardInvocationCaller.connection.call(request) }
    }
}