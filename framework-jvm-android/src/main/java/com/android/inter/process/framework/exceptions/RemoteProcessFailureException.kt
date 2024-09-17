package com.android.inter.process.framework.exceptions

/**
 * @author: liuzhongao
 * @since: 2024/9/16 11:35
 */
class RemoteProcessFailureException : RuntimeException {
    constructor() : super()
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)
    constructor(cause: Throwable?) : super(cause)
    constructor(
        message: String?,
        cause: Throwable?,
        enableSuppression: Boolean,
        writableStackTrace: Boolean
    ) : super(message, cause, enableSuppression, writableStackTrace)

    companion object {
        private const val serialVersionUID: Long = 155513409405070159L
    }
}