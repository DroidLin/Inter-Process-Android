package com.android.inter.process.compiler.dsl

@DslMarker
@Target(AnnotationTarget.CLASS)
annotation class ClassBuilderDSL

@ClassBuilderDSL
internal interface ClassFileGenerationScope {
    fun packageName(value: String)
    fun import(value: String)
    fun classContent(className: String, function: ClassBuilderScope.() -> Unit)
}

@ClassBuilderDSL
internal interface ClassBuilderScope {
    fun mutableProperty(function: PropertyBuilderScope.() -> Unit)
}

@ClassBuilderDSL
internal interface PropertyBuilderScope {
    fun extension(value: String)
    fun name(value: String)
    fun returnType(value: String)

    fun setter(function: StringBuilder.() -> Unit)
    fun getter(function: StringBuilder.() -> Unit)
}