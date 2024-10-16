package com.android.inter.process.framework

import android.os.IBinder
import android.os.RemoteException
import com.android.inter.process.framework.exceptions.BinderNotConnectedException
import com.android.inter.process.framework.exceptions.RemoteProcessFailureException
import com.android.inter.process.framework.metadata.FunctionParameters
import com.android.inter.process.framework.metadata.request
import com.android.inter.process.framework.metadata.response
import java.util.LinkedList

/**
 * delegate of [IFunction]
 *
 * @author: liuzhongao
 * @since: 2024/9/15 10:21
 */
internal fun interface AndroidFunction : IFunction<AndroidRequest, AndroidResponse> {

    /**
     * add death listener to this android function.
     */
    fun addDeathListener(deathListener: DeathListener) {}

    /**
     * remove an exist death listener on this android function.
     */
    fun removeDeathListener(deathListener: DeathListener) {}

    override fun call(request: AndroidRequest): AndroidResponse

    fun interface DeathListener {

        fun onDead()
    }
}

/**
 * fast way to get aidl function [IFunction] instance.
 */
internal val AndroidFunction.function: Function
    get() = when (this) {
        is FunctionCaller -> this.binderFunction
        is FunctionReceiver -> this.binderFunction
        else -> throw IllegalArgumentException("unknown AndroidFunction Type: ${this.javaClass}")
    }

internal fun AndroidFunction(function: Function): AndroidFunction {
    return FunctionCaller(function)
}

internal fun AndroidFunction(function: AndroidFunction): AndroidFunction {
    return FunctionReceiver(function)
}

/**
 * implementation of [AndroidFunction] for calling remote method.
 */
private class FunctionCaller(val binderFunction: Function) : AndroidFunction {

    private val deathListenerList = LinkedList<AndroidFunction.DeathListener>()

    init {
        this.binderFunction.asBinder().linkToDeath(
            object : IBinder.DeathRecipient {
                override fun binderDied() {
                    this@FunctionCaller.binderFunction.asBinder().unlinkToDeath(this, 0)
                    this@FunctionCaller.notifyAndroidFunctionDeath()
                }
            },
            0
        )
    }

    override fun addDeathListener(deathListener: AndroidFunction.DeathListener) {
        synchronized(this.deathListenerList) {
            if (!this.deathListenerList.contains(deathListener)) {
                this.deathListenerList += deathListener
            }
        }
    }

    override fun removeDeathListener(deathListener: AndroidFunction.DeathListener) {
        synchronized(this.deathListenerList) {
            if (this.deathListenerList.contains(deathListener)) {
                this.deathListenerList -= deathListener
            }
        }
    }

    private fun notifyAndroidFunctionDeath() {
        val deathListenerList = synchronized(this.deathListenerList) {
            this.deathListenerList.toList()
        }
        deathListenerList.forEach(AndroidFunction.DeathListener::onDead)
    }

    override fun call(request: AndroidRequest): AndroidResponse {
        InterProcessLogger.logDebug(">>>>>>>>>>>>>>>>>>>>>>>>> Start Inter Process Call <<<<<<<<<<<<<<<<<<<<<<<<<<<")
        var response: AndroidResponse? = null
        when {
            !this.binderFunction.asBinder().pingBinder() -> {
                InterProcessLogger.logDebug("remote process not connected yet.")
                response = DefaultResponse(
                    data = null,
                    throwable = BinderNotConnectedException("binder not connected yet.")
                )
            }

            else -> {
                val functionParameters = FunctionParameters.obtain()
                try {
                    functionParameters.request = request
                    InterProcessLogger.logDebug("calling ${request.javaClass.name}")
                    this.binderFunction.call(functionParameters)
                    response = functionParameters.response
                    if (response == null) {
                        InterProcessLogger.logDebug("fail to get response after request call: ${request.javaClass}")
                        response = DefaultResponse(null, null)
                    }
                } catch (e: RemoteException) {
                    InterProcessLogger.logError(e)
                    response = DefaultResponse(null, RemoteProcessFailureException(e.cause))
                } catch (throwable: Throwable) {
                    InterProcessLogger.logError(throwable)
                    response = DefaultResponse(null, throwable)
                } finally {
                    functionParameters.recycle()
                }
            }
        }
        InterProcessLogger.logDebug(">>>>>>>>>>>>>>>>>>>>>>>>> End Inter Process Call <<<<<<<<<<<<<<<<<<<<<<<<<")
        return requireNotNull(response)
    }
}

private class FunctionReceiver(androidFunction: AndroidFunction) :
    AndroidFunction by androidFunction {

    val binderFunction = object : Function.Stub() {
        override fun call(functionParameters: FunctionParameters?) {
            try {
                InterProcessLogger.logDebug("========================= Receiver Start Inter Process Call =========================")
                val request = functionParameters?.request?.also {
                    functionParameters.request = null
                } ?: return
                InterProcessLogger.logDebug("receive calling ${request.javaClass}")
                val response = this@FunctionReceiver.call(request)
                functionParameters.response = response
            } finally {
                InterProcessLogger.logDebug("========================= Receiver Start Inter Process Call =========================")
            }
        }
    }
}