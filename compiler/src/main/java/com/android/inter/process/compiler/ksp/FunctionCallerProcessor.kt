package com.android.inter.process.compiler.ksp

import com.android.inter.process.framework.Address
import com.android.inter.process.framework.FunctionCallAdapter
import com.android.inter.process.framework.JvmMethodRequest
import com.android.inter.process.framework.annotation.IPCFunction
import com.android.inter.process.framework.annotation.IPCService
import com.google.devtools.ksp.isAbstract
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration

internal fun processCallerFunction(
    resolver: Resolver,
    codeGenerator: CodeGenerator
) {
    val ksAnnotatedList = resolver.getSymbolsWithAnnotation(IPCService::class.java.name).toList()
    val classDeclarations = ksAnnotatedList.findAnnotatedClassDeclarations()
    val classStringList = classDeclarations.map(::buildCallerStructure)

    classStringList.forEachIndexed { index, s ->
        codeGenerator.createNewFile(
            dependencies = Dependencies(
                aggregating = true,
                sources = arrayOf(requireNotNull(classDeclarations[index].containingFile))
            ),
            packageName = classDeclarations[index].classPackageName,
            fileName = classDeclarations[index].callerFileName
        ).bufferedWriter()
            .use { it.append(s).flush() }
    }
}

private fun buildCallerStructure(classDeclaration: KSClassDeclaration): String {
    require(classDeclaration.classKind == ClassKind.INTERFACE)

    val packageName = classDeclaration.packageName.asString()
    val importNames = arrayListOf<String>()
    val addImport = { importName: String ->
        if (importName.isNotEmpty() && !importNames.contains(importName)) {
            importNames += importName
        }
    }
    addImport(Address::class.java.name)
    addImport(FunctionCallAdapter::class.java.name)
    addImport(requireNotNull(classDeclaration.qualifiedName).asString())

    val bodyDeclarations = arrayListOf<StringBuilder>()
    classDeclaration.buildCallerStructureLeafNode(
        buildProperty = { ksPropertyDeclaration ->
            ksPropertyDeclaration.extensionReceiver?.let { ksTypeReference ->
                addImport(ksTypeReference.qualifiedName)
            }
            addImport(ksPropertyDeclaration.type.qualifiedName)
            addImport("com.android.inter.process.framework.syncCall")
            addImport("com.android.inter.process.framework.request")
            val bodyDeclarationBuilder = StringBuilder()
            bodyDeclarationBuilder.appendLine("\toverride ${if (ksPropertyDeclaration.isMutable) "var" else "val"} ${if (ksPropertyDeclaration.extensionReceiver != null) "${requireNotNull(ksPropertyDeclaration.extensionReceiver).simpleName}." else ""}${ksPropertyDeclaration.simpleName.asString()}: ${ksPropertyDeclaration.type.simpleName}")
            if (ksPropertyDeclaration.isMutable) {
                bodyDeclarationBuilder
                    .appendLine("\t\tset(value) {")
                    .appendLine("\t\t\tthis@${classDeclaration.simpleName.asString()}Caller.transformer.syncCall(")
                    .appendLine("\t\t\t\trequest = request(")
                    .appendLine("\t\t\t\t\tclazz = ${classDeclaration.simpleName.asString()}::class.java,")
                    .appendLine("\t\t\t\t\tfunctionIdentifier = \"${ksPropertyDeclaration.identifier(isSetter = true)}\",")
                    .appendLine("\t\t\t\t\tfunctionParameters = listOf(${ksPropertyDeclaration.extensionReceiver?.let { "this, " } ?: ""}value),")
                    .appendLine("\t\t\t\t\thostUniqueKey = this@${classDeclaration.simpleName.asString()}Caller.uniqueKey,")
                    .appendLine("\t\t\t\t\tisSuspend = false")
                    .appendLine("\t\t\t\t)")
                    .appendLine("\t\t\t)")
                    .appendLine("\t\t}")
            }
            bodyDeclarationBuilder
                .appendLine("\t\tget() {")
                .appendLine("\t\t\treturn this@${classDeclaration.simpleName.asString()}Caller.transformer.syncCall(")
                .appendLine("\t\t\t\trequest = request(")
                .appendLine("\t\t\t\t\tclazz = ${classDeclaration.simpleName.asString()}::class.java,")
                .appendLine("\t\t\t\t\tfunctionIdentifier = \"${ksPropertyDeclaration.identifier(isGetter = true)}\",")
                .appendLine("\t\t\t\t\tfunctionParameters = listOf(${ksPropertyDeclaration.extensionReceiver?.let { "this" } ?: ""}),")
                .appendLine("\t\t\t\t\thostUniqueKey = this@${classDeclaration.simpleName.asString()}Caller.uniqueKey,")
                .appendLine("\t\t\t\t\tisSuspend = false")
                .appendLine("\t\t\t\t)")
                .appendLine("\t\t\t) ${if (ksPropertyDeclaration.type.resolve().isMarkedNullable) "as? ${ksPropertyDeclaration.type.typeOfQualifiedName}" else "as ${ksPropertyDeclaration.type.typeOfQualifiedName}"}")
                .appendLine("\t\t}")
            bodyDeclarationBuilder.appendLine("")

            bodyDeclarations += bodyDeclarationBuilder
        },
        buildFunction = { ksFunctionDeclaration ->
            ksFunctionDeclaration.extensionReceiver?.let { ksTypeReference ->
                addImport(ksTypeReference.qualifiedName)
            }
            addImport("com.android.inter.process.framework.syncCall")
            addImport("com.android.inter.process.framework.request")
            addImport(JvmMethodRequest::class.java.name)
            addImport("com.android.inter.process.framework.metadata.AndroidBinderFunctionMetadata")
            addImport("com.android.inter.process.framework.metadata.newBinderFunctionMetadata")

            val parameters =
                ksFunctionDeclaration.parameters.mapIndexed { index, ksValueParameter ->
                    if (ksValueParameter.annotations.find { it.shortName.asString() == IPCFunction::class.java.simpleName } != null) {
                        "newBinderFunctionMetadata(${ksValueParameter.type.resolve().declaration.simpleName.asString()}::class.java, ${
                            requireNotNull(
                                ksValueParameter.name
                            ).asString()
                        })"
                    } else {
                        requireNotNull(ksValueParameter.name).asString()
                    }
                }.joinToString(separator = ", ")
            val bodyDeclarationBuilder = StringBuilder()
            bodyDeclarationBuilder
                .appendLine("\toverride${if (ksFunctionDeclaration.isSuspend) " suspend" else ""} fun ${ksFunctionDeclaration.functionTypeParameters}${ksFunctionDeclaration.extensionReceiverDeclaration}${ksFunctionDeclaration.simpleName.asString()}${ksFunctionDeclaration.parameters.joinToString(separator = ", ", prefix = "(", postfix = ")") { valueParameter -> addImport(valueParameter.type.qualifiedName);"${requireNotNull(valueParameter.name).asString()}: ${valueParameter.type.simpleName}" }}${ksFunctionDeclaration.returnType?.let { ": ${it.simpleName}" } ?: ""} {")
                .appendLine("\t\t${ksFunctionDeclaration.filterReturnType?.let { "return " } ?: ""}this@${classDeclaration.simpleName.asString()}Caller.transformer.${if (ksFunctionDeclaration.isSuspend) "call" else "syncCall"}(")
                .appendLine("\t\t\trequest = request(")
                .appendLine("\t\t\t\tclazz = ${classDeclaration.simpleName.asString()}::class.java,")
                .appendLine("\t\t\t\tfunctionIdentifier = \"${ksFunctionDeclaration.identifier}\",")
                .appendLine("\t\t\t\tfunctionParameters = listOf(${ksFunctionDeclaration.extensionReceiver?.let { "this, " } ?: ""}${parameters}),")
                .appendLine("\t\t\t\thostUniqueKey = this@${classDeclaration.simpleName.asString()}Caller.uniqueKey,")
                .appendLine("\t\t\t\tisSuspend = ${ksFunctionDeclaration.isSuspend}")
                .appendLine("\t\t\t)")
                .appendLine("\t\t) ${ksFunctionDeclaration.filterReturnType?.let { if (it.resolve().isMarkedNullable) "as? ${it.typeOfQualifiedName}" else "as ${it.typeOfQualifiedName}" } ?: ""}")
                .appendLine("\t}")

            bodyDeclarations += bodyDeclarationBuilder
        }
    )

    val stringBuilder = StringBuilder()
    stringBuilder
        .appendLine("package $packageName")
        .appendLine()
        .apply {
            importNames.sort()
            importNames.forEach { importName ->
                appendLine("import $importName")
            }
        }
        .appendLine()
        .appendLine("internal class ${classDeclaration.simpleName.asString()}Caller constructor(")
        .appendLine("\tprivate val transformer: ${FunctionCallAdapter::class.java.simpleName},")
        .appendLine("\tprivate val uniqueKey: ${String::class.java.simpleName}? = null,")
        .appendLine(") : ${classDeclaration.simpleName.asString()} {")
        .apply {
            bodyDeclarations.forEach { bodyDeclaration ->
                append(bodyDeclaration)
            }
        }
        .appendLine("}")
    return stringBuilder.toString()
}

private inline fun KSClassDeclaration.buildCallerStructureLeafNode(
    buildProperty: (KSPropertyDeclaration) -> Unit,
    buildFunction: (KSFunctionDeclaration) -> Unit,
) {
    declarations.forEach { ksDeclaration ->
        when (ksDeclaration) {
            is KSPropertyDeclaration -> {
                if (ksDeclaration.isAbstract()) {
                    buildProperty(ksDeclaration)
                }
            }

            is KSFunctionDeclaration -> {
                if (ksDeclaration.isAbstract) {
                    buildFunction(ksDeclaration)
                }
            }
        }
    }
}
