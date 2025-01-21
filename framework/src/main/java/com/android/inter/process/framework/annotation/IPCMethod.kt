package com.android.inter.process.framework.annotation

/**
 * method in ipc call for async callbacks, only available for interfaces.
 *
 * sample code:
 * ```
 * @IPCInterface
 * fun interface Callback {
 *     fun callback(data: Any)
 * }
 *
 * @IPCInterface
 * interface MyInterface {
 *     fun requestInformation(
 *         id: String,
 *         @IPCMethod
 *         callback: Callback
 *     )
 * }
 * ```
 */
@Retention(value = AnnotationRetention.SOURCE)
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class IPCMethod
