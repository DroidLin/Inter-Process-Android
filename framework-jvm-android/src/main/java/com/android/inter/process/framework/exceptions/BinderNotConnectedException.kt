package com.android.inter.process.framework.exceptions

/**
 * @author: liuzhongao
 * @since: 2024/9/15 13:05
 */
class BinderNotConnectedException : RuntimeException {
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
        private const val serialVersionUID: Long = 368441489644731758L
    }
}