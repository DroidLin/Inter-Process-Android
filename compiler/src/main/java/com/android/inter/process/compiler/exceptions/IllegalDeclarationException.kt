package com.android.inter.process.compiler.exceptions

/**
 * @author: liuzhongao
 * @since: 2024/10/20 21:56
 */
class IllegalDeclarationException : RuntimeException {

    constructor() : super()
    constructor(p0: String?) : super(p0)
    constructor(p0: String?, p1: Throwable?) : super(p0, p1)
    constructor(p0: Throwable?) : super(p0)
    constructor(p0: String?, p1: Throwable?, p2: Boolean, p3: Boolean) : super(p0, p1, p2, p3)
}