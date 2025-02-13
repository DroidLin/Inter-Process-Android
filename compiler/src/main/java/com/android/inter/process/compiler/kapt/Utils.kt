package com.android.inter.process.compiler.kapt

import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.PackageElement
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.PrimitiveType
import javax.lang.model.type.ReferenceType
import javax.lang.model.type.TypeMirror
import kotlin.coroutines.Continuation

internal val TypeElement.packageName: String?
    get() = (this.enclosingElement as? PackageElement)?.qualifiedName?.toString()

internal val TypeElement.callerFileName: String
    get() = "${this.simpleName}Caller"

internal val TypeElement.receiverFileName: String
    get() = "${this.simpleName}Receiver"

internal val TypeMirror.isValidForFullName: Boolean
    get() = this is ReferenceType

internal val TypeMirror.simpleName: String
    get() {
        if (this is DeclaredType) {
            return this.asElement().simpleName.toString()
        }
        val fullName = this.toString()
        return fullName.substring(fullName.lastIndexOf('.') + 1)
    }

internal val TypeMirror.fullName: String
    get() {
        if (this is DeclaredType) {
            val element = this.asElement()
            val packageName = (element.enclosingElement as? PackageElement)?.qualifiedName?.toString()
            if (!packageName.isNullOrEmpty()) {
                return "${packageName}.${element.simpleName}"
            }
        }
        throw IllegalArgumentException("unsupported TypeMirror: ${this.javaClass.name}")
    }

internal val TypeMirror.packageName: String
    get() {
        if (this is DeclaredType) {
            val packageName = (this.asElement().enclosingElement as? PackageElement)?.qualifiedName?.toString()
            if (!packageName.isNullOrEmpty()) {
                return packageName
            }
        }
        throw IllegalArgumentException("unsupported TypeMirror: ${this.javaClass.name}")
    }

internal val ExecutableElement.identifier: String
    get() {
        val parameters = this.parameters.joinToString { variableElement ->
            "${variableElement.asType().simpleName} ${variableElement.simpleName}"
        }
        return "public final ${this.returnType.simpleName} ${this.simpleName}(${parameters})"
    }

internal val ExecutableElement.isSuspendExecutable: Boolean
    get() = this.parameters.find { it.asType().isValidForFullName && it.asType().simpleName == Continuation::class.java.simpleName } != null

internal val ExecutableElement.parameterNoContinuation: List<VariableElement>
    get() {
        val listOf = this.parameters.toMutableList()
        val iterator = listOf.iterator()
        while (iterator.hasNext()) {
            val variableElement = iterator.next()
            if (variableElement.asType().isValidForFullName && variableElement.asType().simpleName == Continuation::class.java.simpleName) {
                iterator.remove()
                break
            }
        }
        return listOf
    }

internal val ExecutableElement.continuationVariable: VariableElement?
    get() {
        var variableElement: VariableElement? = null
        this.parameters.forEach { element ->
            if (element.asType().isValidForFullName && element.asType().simpleName == Continuation::class.java.simpleName) {
                variableElement = element
            }
        }
        return variableElement
    }

internal val TypeElement.suspendEnclosedElements: List<ExecutableElement>
    get() = this.enclosedElements.filter { it is ExecutableElement && it.isSuspendExecutable }
        .filterIsInstance<ExecutableElement>()

internal val TypeElement.nonSuspendEnclosedElements: List<ExecutableElement>
    get() = this.enclosedElements.filter { it is ExecutableElement && !it.isSuspendExecutable }
        .filterIsInstance<ExecutableElement>()
