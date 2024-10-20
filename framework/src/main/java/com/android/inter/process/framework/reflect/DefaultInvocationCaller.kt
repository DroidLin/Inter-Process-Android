package com.android.inter.process.framework.reflect

import com.android.inter.process.framework.ConnectionCommander
import com.android.inter.process.framework.JvmReflectMethodRequest
import com.android.inter.process.framework.Request
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.Continuation

/**
 * @author: liuzhongao
 * @since: 2024/9/16 15:05
 */
internal class DefaultInvocationCaller(private val connectionCommander: ConnectionCommander) : InvocationCaller {
    override fun invoke(request: JvmReflectMethodRequest): Any? {
        return if (request.continuation != null) {
            (this.connectionCommander::call as Function2<Request, Continuation<Any?>, Any?>).invoke(request, requireNotNull(request.continuation))
        } else runBlocking { this@DefaultInvocationCaller.connectionCommander.call(request) }
    }
}