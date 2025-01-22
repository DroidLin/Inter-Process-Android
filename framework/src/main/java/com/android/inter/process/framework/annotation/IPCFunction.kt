package com.android.inter.process.framework.annotation

/**
 * method in ipc call for async callbacks, only available for interfaces.
 *
 * sample code:
 * ```
 * @IPCService
 * fun interface Callback {
 *     fun callback(data: Any)
 * }
 *
 * @IPCService
 * interface MyInterface {
 *     fun requestInformation(
 *         id: String,
 *         @IPCFunction
 *         callback: Callback
 *     )
 * }
 * ```
 *
 * see [IPCService], [IPCFunction] for more information.
 */
@Retention(value = AnnotationRetention.SOURCE)
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class IPCFunction
