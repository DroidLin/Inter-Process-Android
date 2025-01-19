package com.android.inter.process.compiler.ksp

import com.android.inter.process.framework.Address
import com.android.inter.process.framework.FunctionCallTransformer
import com.android.inter.process.framework.JvmMethodRequest
import com.google.devtools.ksp.isAbstract
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration


internal fun buildCallerFunction(
    ksAnnotatedList: List<KSAnnotated>,
    resolver: Resolver,
    codeGenerator: CodeGenerator
) {
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
    addImport(FunctionCallTransformer::class.java.name)
    addImport(requireNotNull(classDeclaration.qualifiedName).asString())

    val bodyDeclarations = arrayListOf<StringBuilder>()
    classDeclaration.buildCallerStructureLeafNode(
        buildProperty = { ksPropertyDeclaration ->
            ksPropertyDeclaration.extensionReceiver?.let { ksTypeReference ->
                addImport(ksTypeReference.qualifiedName)
            }
            addImport(ksPropertyDeclaration.type.qualifiedName)
            addImport("com.android.inter.process.framework.syncTransform")
            addImport(JvmMethodRequest::class.java.name)
            val bodyDeclarationBuilder = StringBuilder()
            bodyDeclarationBuilder.appendLine("\toverride ${if (ksPropertyDeclaration.isMutable) "var" else "val"} ${if (ksPropertyDeclaration.extensionReceiver != null) "${requireNotNull(ksPropertyDeclaration.extensionReceiver).simpleName}." else ""}${ksPropertyDeclaration.simpleName.asString()}: ${ksPropertyDeclaration.type.simpleName}")
            if (ksPropertyDeclaration.isMutable) {
                bodyDeclarationBuilder
                    .appendLine("\t\tset(value) {")
                    .appendLine("\t\t\tthis@${classDeclaration.simpleName.asString()}Caller.transformer.syncTransform(")
                    .appendLine("\t\t\t\trequest = ${JvmMethodRequest::class.java.simpleName}(")
                    .appendLine("\t\t\t\t\tclazz = ${classDeclaration.simpleName.asString()}::class.java,")
                    .appendLine("\t\t\t\t\tfunctionIdentifier = \"${ksPropertyDeclaration.identifier(isSetter = true)}\",")
                    .appendLine("\t\t\t\t\tfunctionParameters = listOf(${ksPropertyDeclaration.extensionReceiver?.let { "this@${classDeclaration.simpleName.asString()}Caller, " } ?: ""}value),")
                    .appendLine("\t\t\t\t\tisSuspend = false")
                    .appendLine("\t\t\t\t)")
                    .appendLine("\t\t\t)")
                    .appendLine("\t\t}")
            }
            bodyDeclarationBuilder
                .appendLine("\t\tget() {")
                .appendLine("\t\t\treturn this@${classDeclaration.simpleName.asString()}Caller.transformer.syncTransform(")
                .appendLine("\t\t\t\trequest = ${JvmMethodRequest::class.java.simpleName}(")
                .appendLine("\t\t\t\t\tclazz = ${classDeclaration.simpleName.asString()}::class.java,")
                .appendLine("\t\t\t\t\tfunctionIdentifier = \"${ksPropertyDeclaration.identifier(isGetter = true)}\",")
                .appendLine("\t\t\t\t\tfunctionParameters = listOf(${ksPropertyDeclaration.extensionReceiver?.let { "this@${classDeclaration.simpleName.asString()}Caller" } ?: ""}),")
                .appendLine("\t\t\t\t\tisSuspend = false")
                .appendLine("\t\t\t\t)")
                .appendLine("\t\t\t) ${if (ksPropertyDeclaration.type.resolve().isMarkedNullable) "as? ${ksPropertyDeclaration.type.simpleName}" else "as ${ksPropertyDeclaration.type.simpleName}"}")
                .appendLine("\t\t}")
            bodyDeclarationBuilder.appendLine("")

            bodyDeclarations += bodyDeclarationBuilder
        },
        buildFunction = {  ksFunctionDeclaration ->
            ksFunctionDeclaration.extensionReceiver?.let { ksTypeReference ->
                addImport(ksTypeReference.qualifiedName)
            }
            addImport("com.android.inter.process.framework.syncTransform")
            addImport(JvmMethodRequest::class.java.name)

            val bodyDeclarationBuilder = StringBuilder()
            bodyDeclarationBuilder
                .appendLine("\toverride${if (ksFunctionDeclaration.isSuspend) " suspend" else ""} fun ${ksFunctionDeclaration.functionTypeParameters}${ksFunctionDeclaration.extensionReceiverDeclaration}${ksFunctionDeclaration.simpleName.asString()}${ksFunctionDeclaration.parameters.joinToString(separator = ", ", prefix = "(", postfix = ")") { valueParameter -> addImport(valueParameter.type.qualifiedName);"${requireNotNull(valueParameter.name).asString()}: ${valueParameter.type.simpleName}" }}${ksFunctionDeclaration.returnType?.let { ": ${it.simpleName}" } ?: ""} {")
                .appendLine("\t\t${ksFunctionDeclaration.returnType?.let { "return " } ?: ""}this@${classDeclaration.simpleName.asString()}Caller.transformer.${if (ksFunctionDeclaration.isSuspend) "transform" else "syncTransform"}(")
                .appendLine("\t\t\trequest = ${JvmMethodRequest::class.java.simpleName}(")
                .appendLine("\t\t\t\tclazz = ${classDeclaration.simpleName.asString()}::class.java,")
                .appendLine("\t\t\t\tfunctionIdentifier = \"${ksFunctionDeclaration.identifier}\",")
                .appendLine("\t\t\t\tfunctionParameters = listOf(${ksFunctionDeclaration.extensionReceiver?.let { "this@${classDeclaration.simpleName.asString()}Caller, " } ?: ""}${ksFunctionDeclaration.parameters.joinToString(separator = ", ") { requireNotNull(it.name).asString() }}),")
                .appendLine("\t\t\t\tisSuspend = ${ksFunctionDeclaration.isSuspend}")
                .appendLine("\t\t\t)")
                .appendLine("\t\t) ${ksFunctionDeclaration.returnType?.let { if (it.resolve().isMarkedNullable) "as? ${it.simpleName}" else "as ${it.simpleName}" } ?: ""}")
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
        .appendLine("\tprivate val transformer: ${FunctionCallTransformer::class.java.simpleName}")
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
