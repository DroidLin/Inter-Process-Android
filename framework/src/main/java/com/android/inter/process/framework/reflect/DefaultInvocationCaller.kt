package com.android.inter.process.framework.reflect

import com.android.inter.process.framework.FunctionCallTransformer
import com.android.inter.process.framework.JvmReflectMethodRequest
import com.android.inter.process.framework.Request
import com.android.inter.process.framework.syncTransform
import kotlin.coroutines.Continuation

/**
 * @author: liuzhongao
 * @since: 2024/9/16 15:05
 */
internal class DefaultInvocationCaller(private val functionCallTransformer: FunctionCallTransformer) : InvocationCaller {
    override fun invoke(request: JvmReflectMethodRequest): Any? {
        return if (request.continuation != null) {
            (this.functionCallTransformer::transform as Function2<Request, Continuation<Any?>, Any?>).invoke(request, requireNotNull(request.continuation))
        } else functionCallTransformer.syncTransform(request)
    }
}