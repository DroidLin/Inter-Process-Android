package com.android.inter.process.framework.reflect

import com.android.inter.process.framework.FunctionCallAdapter
import com.android.inter.process.framework.Request
import com.android.inter.process.framework.syncCall
import java.lang.reflect.Method
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.Continuation
import com.android.inter.process.framework.reflect.AndroidServiceMethod.AndroidCallerServiceMethod

fun InvocationCallerAndroid(functionCallAdapter: FunctionCallAdapter, hostUniqueKey: String?): InvocationCaller {
    return AndroidInvocationCaller(functionCallAdapter = functionCallAdapter, hostUniqueKey = hostUniqueKey)
}

private class AndroidInvocationCaller(
    private val functionCallAdapter: FunctionCallAdapter,
    private val hostUniqueKey: String?
) : InvocationCaller {

    private val serviceMethodCache by lazy { ConcurrentHashMap<String, AndroidCallerServiceMethod>() }

    override fun invoke(method: Method, parameters: Array<Any?>?): Any? {
        // parameters directly from invocation handler, including suspend function parameter Continuation
        val serviceMethod = this.serviceMethodCache.getOrPut(method.toGenericString()) {
            AndroidServiceMethod.parseCallerServiceMethod(method)
        }
        // for now, service method is running for annotation [IPCMethod],
        // and is created for further use.
        val transformedParameters = serviceMethod.invoke(parameters ?: emptyArray())
        val request = InvocationCaller.parseRequest(this.hostUniqueKey, method, transformedParameters)
        val isSuspend = request.continuation != null
        return if (isSuspend) {
            (this.functionCallAdapter::call as Function2<Request, Continuation<Any?>, Any?>)
                .invoke(request, requireNotNull(request.continuation))
        } else functionCallAdapter.syncCall(request)
    }
}