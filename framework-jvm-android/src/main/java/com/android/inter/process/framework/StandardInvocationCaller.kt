package com.android.inter.process.framework

import com.android.inter.process.framework.metadata.FunctionParameter
import com.android.inter.process.framework.metadata.SuspendContext
import com.android.inter.process.framework.reflect.InvocationCaller
import kotlinx.coroutines.runBlocking
import java.lang.reflect.Method
import kotlin.coroutines.Continuation
import kotlin.coroutines.intrinsics.suspendCoroutineUninterceptedOrReturn

/**
 * @author: liuzhongao
 * @since: 2024/9/16 15:05
 */
internal class StandardInvocationCaller(
    private val tryConnect: (() -> AndroidFunction)? = null,
    private val tryConnectSuspend: (suspend () -> AndroidFunction)? = null
) : InvocationCaller {
    override fun invoke(instance: Any?, method: Method, parameters: Array<Any?>?): Any? {
        val methodParameterTypes: Array<Class<*>> = method.parameterTypes
        val isSuspend = methodParameterTypes.lastOrNull() == Continuation::class.java
        val continuation =
            parameters?.lastOrNull()?.takeIf { it is Continuation<*> } as? Continuation<Any?>

        val newMethodParameterType = (if (isSuspend) {
            methodParameterTypes.copyOf(methodParameterTypes.size - 1)
        } else methodParameterTypes).mapNotNull { it?.name }
        val newMethodParameters = (if (isSuspend && parameters != null) {
            parameters.copyOf(parameters.size - 1)
        } else if (isSuspend) {
            throw IllegalArgumentException("suspend function with null origin parameters in invocation handler.")
        } else parameters)?.toList() ?: emptyList()

        return if (isSuspend) {
            (this::invokeSuspend as Function2<(AndroidFunction, Continuation<*>) -> Unit, Continuation<*>, AndroidResponse>)
                .invoke({ androidFunction, cont ->
                    val functionParameter = FunctionParameter(
                        functionType = IContinuation::class.java,
                        androidFunction = IContinuation::class.java.receiverAndroidFunction(IContinuation(cont))
                    )
                    val request = AndroidRequest(
                        declaredClassFullName = method.declaringClass.name,
                        methodName = method.name,
                        methodParameterTypeFullNames = newMethodParameterType,
                        methodParameterValues = newMethodParameters,
                        uniqueId = "",
                        suspendContext = SuspendContext(functionParameter = functionParameter)
                    )
                    this.invoke(androidFunction, request)
                }, requireNotNull(continuation))
        } else {
            val request = AndroidRequest(
                declaredClassFullName = method.declaringClass.name,
                methodName = method.name,
                methodParameterTypeFullNames = newMethodParameterType,
                methodParameterValues = newMethodParameters,
                uniqueId = "",
            )
            val androidFunction = requireNotNull(
                this.tryConnect?.invoke()
                    ?: runBlocking {
                        this@StandardInvocationCaller.tryConnectSuspend?.invoke()
                    }
            )
            this.invoke(androidFunction, request)
        }
    }

    private suspend fun invokeSuspend(block: (AndroidFunction, Continuation<*>) -> Unit): Any? {
        val function = requireNotNull(this.tryConnect?.invoke() ?: this.tryConnectSuspend?.invoke())
        return suspendCoroutineUninterceptedOrReturn { cont ->
            block(function, IContinuation(cont))
        }
    }

    private fun invoke(androidFunction: AndroidFunction, request: AndroidRequest): Any? {
        val response = BasicConnection(androidFunction).call(request)
        if (response.throwable != null) {
            throw response.throwable
        }
        return response.data
    }
}