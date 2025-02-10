package com.android.inter.process.compiler.ksp

import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSTypeReference
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.google.devtools.ksp.symbol.Modifier
import java.math.BigInteger
import java.security.MessageDigest
import java.util.UUID

internal val KSFunctionDeclaration.filterReturnType: KSTypeReference?
    get() {
        val returnTypeQualifiedName = this.returnType?.qualifiedName ?: return null
        return when (returnTypeQualifiedName) {
            Unit::class.java.name,
            Void::class.java.name,
            Unit::class.javaPrimitiveType?.name,
            Void::class.javaPrimitiveType?.name -> null
            else -> this.returnType
        }
    }

internal val KSTypeReference.qualifiedName: String
    get() = requireNotNull(this.resolve().declaration.qualifiedName).asString()

internal val KSTypeReference.typeOfQualifiedName: String
    get() = "${this.resolve().declaration.packageName.asString()}.${simpleName}"

internal val KSTypeReference.simpleName: String
    get() {
        val ksType = this.resolve()
        return StringBuilder()
            .append(requireNotNull(ksType.declaration.simpleName).asString())
            .apply {
                val typeArguments = ksType.arguments
                if (typeArguments.isNotEmpty()) {
                    try {
                        append("<")
                        typeArguments.forEachIndexed { index, ksTypeArgument ->
                            if (index != 0) {
                                append(", ")
                            }
                            append(requireNotNull(ksTypeArgument.type).typeOfQualifiedName)
                        }
                    } finally {
                        append(">")
                    }
                }
                if (ksType.isMarkedNullable) {
                    append("?")
                }
            }
            .toString()
    }

//internal fun KSTypeReference.simpleName(ignoreTypeParameter: Boolean = false): String {
//
//}
internal val KSFunctionDeclaration.isSuspend: Boolean
    get() = this.modifiers.contains(Modifier.SUSPEND)


internal val KSFunctionDeclaration.identifier: String
    get() {
        val functionParameters = this.parameters
        return "${if (this.isSuspend) "suspend " else "" }fun ${this.extensionReceiver?.let { "${it.simpleName}." } ?: ""}${this.simpleName.asString()}${
            functionParameters.joinToString(separator = ", ", prefix = "(", postfix = ")") { ksValueParameter ->
                "${(requireNotNull(ksValueParameter.name).asString())}: ${ksValueParameter.type.simpleName}"
            }
        }${this.returnType?.let { returnType -> ": ${returnType.simpleName}" } ?: ""}"
    }

internal fun KSPropertyDeclaration.identifier(isSetter: Boolean = false, isGetter: Boolean = false): String {
    if (isSetter) {
        return "setter ${if (this.isMutable) "var" else "val"} ${this.extensionReceiver?.let { "${it.simpleName}." } ?: ""}${this.simpleName.asString()}: ${this.type.simpleName}"
    }
    if (isGetter) {
        return "getter ${if (this.isMutable) "var" else "val"} ${this.extensionReceiver?.let { "${it.simpleName}." } ?: ""}${this.simpleName.asString()}: ${this.type.simpleName}"
    }
    return ""
}

internal val KSFunctionDeclaration.functionTypeParameters: String
    get() = this.typeParameters.takeIf { it.isNotEmpty() }?.let { it.joinToString(", ", prefix = "<", postfix = "> ") { it.simpleName.asString() } } ?: ""

internal val KSFunctionDeclaration.extensionReceiverDeclaration: String
    get() = this.extensionReceiver?.let { "${it.simpleName}." } ?: ""


internal fun List<KSAnnotated>.findAnnotatedClassDeclarations(): List<KSClassDeclaration> {
    val list = arrayListOf<KSClassDeclaration>()
    forEach { ksAnnotated ->
        val visitor = object : KSVisitorVoid() {
            override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
                list += classDeclaration
            }
        }
        ksAnnotated.accept(visitor, Unit)
    }
    return list
}

internal val KSClassDeclaration.classPackageName: String
    get() = packageName.asString()

internal val KSClassDeclaration.callerClassName: String
    get() = "${classPackageName}.${callerFileName}"

internal val KSClassDeclaration.callerFileName: String
    get() = "${simpleName.asString()}Caller"

internal val KSClassDeclaration.receiverClassName: String
    get() = "${classPackageName}.${receiverFileName}"

internal val KSClassDeclaration.receiverFileName: String
    get() = "${simpleName.asString()}Receiver"

internal val GeneratedPackageName: String
    get() = "com.android.inter.process.generated"

internal fun StringBuilder.import(className: String): StringBuilder = apply {
    append("import").append(' ').appendLine(className)
}