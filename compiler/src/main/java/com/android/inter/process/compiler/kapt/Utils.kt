package com.android.inter.process.compiler.kapt

import javax.lang.model.element.PackageElement
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.lang.model.type.TypeMirror

internal val TypeElement.packageName: String?
    get() = (this.enclosingElement as? PackageElement)?.qualifiedName?.toString()

internal val TypeElement.callerFileName: String
    get() = "${this.simpleName}Caller"

internal val VariableElement.typeFullName: String
    get() {
        val typeElement = this.asType()
        return StringBuilder()
            .append(this.simpleName)
            .apply {
                val typeArguments = typeElement
            }
            .toString()
    }

internal val TypeMirror.simpleName: String
    get() {
        val fullName = this.toString()
        return fullName.substring(fullName.lastIndexOf('.') + 1)
    }

internal val TypeMirror.fullName: String
    get() = this.toString()

internal val TypeMirror.packageName: String
    get() {
        val fullName = this.toString()
        return fullName.substring(0, fullName.lastIndexOf('.'))
    }