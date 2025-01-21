package com.android.inter.process.framework

import java.io.FileDescriptor

/**
 * @author: liuzhongao
 * @since: 2024/9/15 11:11
 */

val defaultReturnType: List<Class<*>> = listOfNotNull(
    Void::class.java,
    Void::class.javaPrimitiveType,
    Unit::class.java,
    Unit::class.javaPrimitiveType
)

fun Any?.typeSafe(): Any? {
    if (this == null) return null
    if (this.javaClass in defaultReturnType) return null
    return this
}

val Array<String>.stringType2ClassType: Array<Class<*>>
    get() = this.map { className -> className.stringType2ClassType }.toTypedArray()

val List<String>.stringType2ClassType: List<Class<*>>
    get() = this.map { className -> className.stringType2ClassType }

val String.stringType2ClassType: Class<*>
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
