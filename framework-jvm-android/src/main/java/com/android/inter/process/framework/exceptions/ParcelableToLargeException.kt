package com.android.inter.process.framework.exceptions

/**
 * @author liuzhongao
 * @since 2025/1/21 16:42
 */
class ParcelableToLargeException : RuntimeException {

    constructor() : super()
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)
    constructor(cause: Throwable?) : super(cause)

    companion object {
        private const val serialVersionUID: Long = -5782157412420078193L
    }
}