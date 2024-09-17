package com.android.inter.process.framework

import com.android.inter.process.framework.address.AndroidAddress
import com.android.inter.process.framework.connector.AndroidConnectorFactory
import com.android.inter.process.framework.metadata.ConnectContext
import com.android.inter.process.framework.reflect.InvocationParameter
import com.android.inter.process.framework.reflect.InvocationReceiver
import kotlinx.coroutines.runBlocking
import java.util.concurrent.atomic.AtomicInteger

/**
 * basic interface used to get information about remote process,
 * including version of inter-process-calling tool-kit, package name and etc.
 *
 * for further communication operations, two-way connection binding is also included.
 *
 * @author: liuzhongao
 * @since: 2024/9/16 12:28
 */
internal interface BasicConnection : IFunction<AndroidRequest, AndroidResponse> {

    /**
     * get version of [BasicConnection] tool kit.
     *
     * encode method like: (000)(000)(000).
     *
     * example: (1.2.1) -> (001002001), (1.20.1) -> (001020001)
     */
    val version: Long

    /**
     * set handle to remote process.
     */
    fun setConnectContext(connectContext: ConnectContext)
}

private val METHOD_COUNTER = AtomicInteger()
private val unique_id_version = METHOD_COUNTER.incrementAndGet().toString()
private val unique_id_set_connect_context = METHOD_COUNTER.incrementAndGet().toString()
private val unique_id_call = METHOD_COUNTER.incrementAndGet().toString()

internal fun BasicConnection(androidAddress: AndroidAddress): BasicConnection {
    return BasicConnectionCaller(androidAddress)
}

internal fun BasicConnection(androidFunction: AndroidFunction): BasicConnection {
    return BasicConnectionCaller(androidFunction)
}

internal fun BasicConnection(): InvocationReceiver<BasicConnection> {
    return BasicConnectionReceiver()
}

private class BasicConnectionCaller(private val tryConnect: suspend () -> AndroidFunction) :
    BasicConnection {

    constructor(androidAddress: AndroidAddress) : this(
        tryConnect = {
            AndroidConnectorFactory.connectorCreate(address = androidAddress).tryConnect()
        }
    )

    constructor(androidFunction: AndroidFunction) : this({ androidFunction })

    override val version: Long
        get() {
            return runBlocking {
                this@BasicConnectionCaller.tryConnect().call(
                    request = AndroidRequest(
                        declaredClassFullName = BasicConnection::class.java.name,
                        methodName = "",
                        methodParameterTypeFullNames = emptyList(),
                        methodParameterValues = emptyList(),
                        uniqueId = unique_id_version,
                        suspendContext = null
                    )
                ).getOrThrow() as Long
            }
        }

    override fun setConnectContext(connectContext: ConnectContext) {
        runBlocking {
            this@BasicConnectionCaller.tryConnect().call(
                request = AndroidRequest(
                    declaredClassFullName = BasicConnection::class.java.name,
                    methodName = "",
                    methodParameterTypeFullNames = emptyList(),
                    methodParameterValues = listOf(connectContext),
                    uniqueId = unique_id_set_connect_context,
                    suspendContext = null
                )
            )
        }
    }

    override fun call(parameter: AndroidRequest): AndroidResponse {
        return runBlocking {
            this@BasicConnectionCaller.tryConnect().call(
                request = AndroidRequest(
                    declaredClassFullName = BasicConnection::class.java.name,
                    methodName = "",
                    methodParameterTypeFullNames = emptyList(),
                    methodParameterValues = listOf(parameter),
                    uniqueId = unique_id_call,
                    suspendContext = null
                )
            )
        }
    }
}

private class BasicConnectionReceiver : InvocationReceiver<BasicConnection> {
    override fun invoke(instance: BasicConnection, invocationParameter: InvocationParameter): Any? {
        return when (invocationParameter.uniqueKey) {
            unique_id_version -> instance.version
            unique_id_set_connect_context -> instance.setConnectContext(invocationParameter.methodParameterValues[0] as ConnectContext)
            unique_id_call -> instance.call(invocationParameter.methodParameterValues[0] as AndroidRequest)
            else -> null
        }
    }
}