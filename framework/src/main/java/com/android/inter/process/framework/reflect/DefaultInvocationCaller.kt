package com.android.inter.process.framework.reflect

import com.android.inter.process.framework.FunctionCallAdapter
import com.android.inter.process.framework.Request
import com.android.inter.process.framework.syncCall
import java.lang.reflect.Method
import kotlin.coroutines.Continuation

/**
 * @author: liuzhongao
 * @since: 2024/9/16 15:05
 */
internal class DefaultInvocationCaller(
    private val functionCallAdapter: FunctionCallAdapter,
    private val uniqueKey: String?
) : InvocationCaller {

    override fun invoke(method: Method, parameters: Array<Any?>?): Any? {
        val request = InvocationCaller.parseRequest(this.uniqueKey, method, parameters)
        val isSuspend = request.continuation != null
        return if (isSuspend) {
            (this.functionCallAdapter::call as Function2<Request, Continuation<Any?>, Any?>).invoke(request, requireNotNull(request.continuation))
        } else functionCallAdapter.syncCall(request)
    }
}