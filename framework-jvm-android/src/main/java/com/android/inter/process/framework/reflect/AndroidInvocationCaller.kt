package com.android.inter.process.framework.reflect

import com.android.inter.process.framework.FunctionCallTransformer
import com.android.inter.process.framework.Request
import com.android.inter.process.framework.syncTransform
import java.lang.reflect.Method
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.Continuation
import com.android.inter.process.framework.reflect.AndroidServiceMethod.AndroidCallerServiceMethod

fun InvocationCallerAndroid(functionCallTransformer: FunctionCallTransformer): InvocationCaller {
    return AndroidInvocationCaller(functionCallTransformer = functionCallTransformer)
}

private class AndroidInvocationCaller(
    private val functionCallTransformer: FunctionCallTransformer
) : InvocationCaller {

    private val serviceMethodCache by lazy { ConcurrentHashMap<String, AndroidCallerServiceMethod>() }

    override fun invoke(method: Method, parameters: Array<Any?>?): Any? {
        val serviceMethod = this.serviceMethodCache.getOrPut(method.toGenericString()) {
            AndroidServiceMethod.parseCallerServiceMethod(method)
        }
        val transformedParameters = serviceMethod.invoke(parameters ?: emptyArray())
        val request = InvocationCaller.parseRequest(method, transformedParameters)
        val isSuspend = request.continuation != null
        return if (isSuspend) {
            (this.functionCallTransformer::transform as Function2<Request, Continuation<Any?>, Any?>)
                .invoke(request, requireNotNull(request.continuation))
        } else functionCallTransformer.syncTransform(request)
    }
}