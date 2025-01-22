package com.android.inter.process.compiler.ksp

import com.android.inter.process.compiler.exceptions.IllegalDeclarationException
import com.android.inter.process.framework.annotation.IPCMethod
import com.android.inter.process.framework.reflect.InvocationParameter
import com.android.inter.process.framework.reflect.InvocationReceiver
import com.google.devtools.ksp.isAbstract
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration


internal fun buildReceiverFunction(
    ksAnnotatedList: List<KSAnnotated>,
    resolver: Resolver,
    codeGenerator: CodeGenerator
) {
    val classDeclarations = ksAnnotatedList.findAnnotatedClassDeclarations()
    val classStringList = classDeclarations.map(::buildReceiverStructure)

    classStringList.forEachIndexed { index, s ->
        codeGenerator.createNewFile(
            dependencies = Dependencies(
                aggregating = true,
                sources = arrayOf(requireNotNull(classDeclarations[index].containingFile))
            ),
            packageName = classDeclarations[index].classPackageName,
            fileName = classDeclarations[index].receiverFileName
        ).bufferedWriter()
            .use { it.append(s).flush() }
    }
}

private fun buildReceiverStructure(classDeclaration: KSClassDeclaration): String {
    val packageName = classDeclaration.packageName.asString()
    val importNames = arrayListOf<String>()
    val addImport = { importName: String ->
        if (!importNames.contains(importName)) {
            importNames += importName
        }
    }

    addImport(requireNotNull(classDeclaration.qualifiedName).asString())
    addImport(InvocationReceiver::class.java.name)
    addImport(InvocationParameter::class.java.name)
    addImport("com.android.inter.process.framework.metadata.AndroidBinderFunctionParameter")
    addImport("com.android.inter.process.framework.metadata.function")

    val kotlinStringBuilder = StringBuilder()
    kotlinStringBuilder
        .appendLine("package $packageName")
        .appendLine()
        .appendLine(importNames.joinToString("\n") { "import $it" })
        .appendLine()
        .appendLine("internal class ${classDeclaration.simpleName.asString()}Receiver constructor(private val rawInstance: ${classDeclaration.simpleName.asString()}): ${InvocationReceiver::class.java.simpleName}<${classDeclaration.simpleName.asString()}> {")
        .appendLine()
        .appendLine("\toverride fun invoke(invocationParameter: InvocationParameter): Any? {")
        .also { stringBuilder ->
            stringBuilder
                .appendLine("\t\tval functionParameters = invocationParameter.methodParameterValues")
                .appendLine("\t\treturn when (invocationParameter.uniqueKey) {")
                .also { builder ->
                    val abstractKotlinPropertyDeclaration = classDeclaration.declarations.filter { it is KSPropertyDeclaration && it.isAbstract() } as Sequence<KSPropertyDeclaration>
                    abstractKotlinPropertyDeclaration.forEach { ksPropertyDeclaration ->
                        if (ksPropertyDeclaration.isMutable) {
                            val extensionReceiver = ksPropertyDeclaration.extensionReceiver
                            if (extensionReceiver != null) {
                                builder.appendLine("\t\t\t\"${ksPropertyDeclaration.identifier(isSetter = true)}\" -> this@${classDeclaration.simpleName.asString()}Receiver.rawInstance.run { (functionParameters[0] ${if (extensionReceiver.resolve().isMarkedNullable) "as?" else "as"} ${extensionReceiver.typeOfQualifiedName}).${ksPropertyDeclaration.simpleName.asString()} = (functionParameters[0] ${if (ksPropertyDeclaration.type.resolve().isMarkedNullable) "as?" else "as"} ${ksPropertyDeclaration.type.typeOfQualifiedName}) }")
                            } else {
                                builder.appendLine("\t\t\t\"${ksPropertyDeclaration.identifier(isSetter = true)}\" -> this@${classDeclaration.simpleName.asString()}Receiver.rawInstance = (functionParameters[0] ${if (ksPropertyDeclaration.type.resolve().isMarkedNullable) "as?" else "as"} ${ksPropertyDeclaration.type.typeOfQualifiedName}})")
                            }
                        } else {
                            val extensionReceiver = ksPropertyDeclaration.extensionReceiver
                            if (extensionReceiver != null) {
                                builder.appendLine("\t\t\t\"${ksPropertyDeclaration.identifier(isGetter = true)}\" -> this@${classDeclaration.simpleName.asString()}Receiver.rawInstance.run { (functionParameters[0] ${if (extensionReceiver.resolve().isMarkedNullable) "as?" else "as"} ${extensionReceiver.typeOfQualifiedName}).${ksPropertyDeclaration.simpleName.asString()} }")
                            } else {
                                builder.appendLine("\t\t\t\"${ksPropertyDeclaration.identifier(isGetter = true)}\" -> this@${classDeclaration.simpleName.asString()}Receiver.rawInstance.${ksPropertyDeclaration.simpleName.asString()}")
                            }
                        }
                    }
                    val abstractKotlinNonSuspendFunctionDeclaration = classDeclaration.declarations.filter { it is KSFunctionDeclaration && it.isAbstract && !it.isSuspend } as Sequence<KSFunctionDeclaration>
                    abstractKotlinNonSuspendFunctionDeclaration.forEach { ksFunctionDeclaration ->
                        val extensionReceiver = ksFunctionDeclaration.extensionReceiver
                        if (extensionReceiver != null) {
                            val parameters = ksFunctionDeclaration.parameters.mapIndexed { index, ksValueParameter ->
                                if (ksValueParameter.annotations.find { it.shortName.asString() == IPCMethod::class.java.simpleName } != null) {
                                    "(functionParameters[${index + 1}] ${if (ksValueParameter.type.resolve().isMarkedNullable) "as?" else "as"} AndroidBinderFunctionParameter)${if (ksValueParameter.type.resolve().isMarkedNullable) "?" else ""}.function<${ksValueParameter.type.typeOfQualifiedName}>()"
                                } else {
                                    "(functionParameters[${index + 1}] ${if (ksValueParameter.type.resolve().isMarkedNullable) "as?" else "as"} ${ksValueParameter.type.typeOfQualifiedName})"
                                }
                            }
                            builder.appendLine("\t\t\t\"${ksFunctionDeclaration.identifier}\" -> this@${classDeclaration.simpleName.asString()}Receiver.rawInstance.run { (functionParameters[0] ${if (extensionReceiver.resolve().isMarkedNullable) "as?" else "as"} ${extensionReceiver.typeOfQualifiedName}).${ksFunctionDeclaration.simpleName.asString()}${parameters.joinToString(separator = ", ", prefix = "(", postfix = ")")} }")
                        } else {
                            val parameters = ksFunctionDeclaration.parameters.mapIndexed { index, ksValueParameter ->
                                if (ksValueParameter.annotations.find { it.shortName.asString() == IPCMethod::class.java.simpleName } != null) {
                                    "(functionParameters[${index}] ${if (ksValueParameter.type.resolve().isMarkedNullable) "as?" else "as"} AndroidBinderFunctionParameter)${if (ksValueParameter.type.resolve().isMarkedNullable) "?" else ""}.function<${ksValueParameter.type.typeOfQualifiedName}>()"
                                } else {
                                    "(functionParameters[${index}] ${if (ksValueParameter.type.resolve().isMarkedNullable) "as?" else "as"} ${ksValueParameter.type.typeOfQualifiedName})"
                                }
                            }
                            builder.appendLine("\t\t\t\"${ksFunctionDeclaration.identifier}\" -> this@${classDeclaration.simpleName.asString()}Receiver.rawInstance.${ksFunctionDeclaration.simpleName.asString()}${parameters.joinToString(separator = ", ", prefix = "(", postfix = ")")}")
                        }
                    }
                }
                .appendLine("\t\t\telse -> null")
                .appendLine("\t\t}")
        }
        .appendLine("\t}")
        .appendLine()
        .appendLine("\toverride suspend fun invokeSuspend(invocationParameter: InvocationParameter): Any? {")
        .also { stringBuilder ->
            stringBuilder
                .appendLine("\t\tval functionParameters = invocationParameter.methodParameterValues")
                .appendLine("\t\treturn when (invocationParameter.uniqueKey) {")
                .also { builder ->
                    val abstractKotlinSuspendFunctionDeclaration = classDeclaration.declarations.filter { it is KSFunctionDeclaration && it.isAbstract && it.isSuspend } as Sequence<KSFunctionDeclaration>
                    abstractKotlinSuspendFunctionDeclaration.forEach { ksFunctionDeclaration ->
                        if (ksFunctionDeclaration.typeParameters.isNotEmpty()) {
                            throw IllegalDeclarationException("type parameters declared in function ${requireNotNull(ksFunctionDeclaration.qualifiedName).asString()} is not allowed!!!")
                        }
                        val extensionReceiver = ksFunctionDeclaration.extensionReceiver
                        if (extensionReceiver != null) {
                            val parameters = ksFunctionDeclaration.parameters.mapIndexed { index, ksValueParameter ->
                                if (ksValueParameter.annotations.find { it.shortName.asString() == IPCMethod::class.java.simpleName } != null) {
                                    "(functionParameters[${index + 1}] ${if (ksValueParameter.type.resolve().isMarkedNullable) "as?" else "as"} AndroidBinderFunctionParameter)${if (ksValueParameter.type.resolve().isMarkedNullable) "?" else ""}.function<${ksValueParameter.type.typeOfQualifiedName}>()"
                                } else {
                                    "(functionParameters[${index + 1}] ${if (ksValueParameter.type.resolve().isMarkedNullable) "as?" else "as"} ${ksValueParameter.type.typeOfQualifiedName})"
                                }
                            }
                            builder.appendLine("\t\t\t\"${ksFunctionDeclaration.identifier}\" -> this@${classDeclaration.simpleName.asString()}Receiver.rawInstance.run { (functionParameters[0] ${if (extensionReceiver.resolve().isMarkedNullable) "as?" else "as"} ${extensionReceiver.typeOfQualifiedName}).${ksFunctionDeclaration.simpleName.asString()}${parameters.joinToString(separator = ", ", prefix = "(", postfix = ")")} }")
                        } else {
                            val parameters = ksFunctionDeclaration.parameters.mapIndexed { index, ksValueParameter ->
                                if (ksValueParameter.annotations.find { it.shortName.asString() == IPCMethod::class.java.simpleName } != null) {
                                    "(functionParameters[${index}] ${if (ksValueParameter.type.resolve().isMarkedNullable) "as?" else "as"} AndroidBinderFunctionParameter)${if (ksValueParameter.type.resolve().isMarkedNullable) "?" else ""}.function<${ksValueParameter.type.typeOfQualifiedName}>()"
                                } else {
                                    "(functionParameters[${index}] ${if (ksValueParameter.type.resolve().isMarkedNullable) "as?" else "as"} ${ksValueParameter.type.typeOfQualifiedName})"
                                }
                            }
                            builder.appendLine("\t\t\t\"${ksFunctionDeclaration.identifier}\" -> this@${classDeclaration.simpleName.asString()}Receiver.rawInstance.${ksFunctionDeclaration.simpleName.asString()}${parameters.joinToString(separator = ", ", prefix = "(", postfix = ")")}")
                        }
                    }
                }
                .appendLine("\t\t\telse -> null")
                .appendLine("\t\t}")
        }
        .appendLine("\t}")
        .appendLine("}")

    return kotlinStringBuilder.toString()
}