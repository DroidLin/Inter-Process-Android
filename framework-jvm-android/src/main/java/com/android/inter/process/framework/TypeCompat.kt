package com.android.inter.process.framework

import java.io.FileDescriptor

/**
 * @author: liuzhongao
 * @since: 2024/9/15 11:11
 */

internal val defaultReturnType: List<Class<*>> = listOfNotNull(
    Void::class.java,
    Void::class.javaPrimitiveType,
    Unit::class.java,
    Unit::class.javaPrimitiveType
)

internal fun Any?.typeSafe(): Any? {
    if (this == null) return null
    if (this.javaClass in defaultReturnType) return null
    return this
}

internal fun Any?.containsFileDescriptor(): Boolean {
    if (this == null) return false
    if (this is FileDescriptor) return true
    return when (this) {
        is Map<*, *> -> this.any { (key, value) -> (key.containsFileDescriptor() || value.containsFileDescriptor()) }
        is Iterable<*> -> this.any { it.containsFileDescriptor() }
        is Array<*> -> this.any { it.containsFileDescriptor() }
        else -> false
    }
}

internal val Array<String>.stringTypeConvert: Array<Class<*>>
    get() = this.map { className -> className.stringTypeConvert }.toTypedArray()

internal val List<String>.stringTypeConvert: List<Class<*>>
    get() = this.map { className -> className.stringTypeConvert }

internal val String.stringTypeConvert: Class<*>
    get() = when (this) {
        Byte::class.java.name -> Byte::class.java
        Int::class.java.name -> Int::class.java
        Short::class.java.name -> Short::class.java
        Long::class.java.name -> Long::class.java
        Float::class.java.name -> Float::class.java
        Double::class.java.name -> Double::class.java
        Boolean::class.java.name -> Boolean::class.java
        Char::class.java.name -> Char::class.java
        else -> Class.forName(this)
    }
