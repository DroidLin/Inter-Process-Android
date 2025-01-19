package com.android.inter.process.compiler.ksp

import com.android.inter.process.framework.ObjectPool
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSAnnotated

internal fun buildFunctionCollector(
    ksAnnotatedList: List<KSAnnotated>,
    resolver: Resolver,
    codeGenerator: CodeGenerator
) {
    if (ksAnnotatedList.isEmpty()) return
    val classDeclarations = ksAnnotatedList.findAnnotatedClassDeclarations()
    val generatedClassName = "${ObjectPool.Collector::class.java.simpleName}_${RandomHash}"

    val stringBuilder = StringBuilder()

    stringBuilder.appendLine("package $GeneratedPackageName")
    stringBuilder.appendLine()
    classDeclarations.forEach { classDeclaration ->
        stringBuilder.import(requireNotNull(classDeclaration.qualifiedName).asString())
        stringBuilder.import(classDeclaration.callerClassName)
        stringBuilder.import(classDeclaration.receiverClassName)
    }
    stringBuilder.import("com.android.inter.process.framework.objectPool")
    stringBuilder.import("com.android.inter.process.framework.ObjectPool.Collector")
    stringBuilder.appendLine()
    stringBuilder.appendLine("internal class $generatedClassName : ${ObjectPool.Collector::class.java.simpleName} {")
    stringBuilder.appendLine()
    stringBuilder.append('\t').appendLine("override fun collect() {")
    classDeclarations.forEach { ksClassDeclaration ->
        stringBuilder.append('\t').append('\t').appendLine("objectPool.putCallerBuilder(${ksClassDeclaration.simpleName.asString()}::class.java) { ${ksClassDeclaration.callerFileName}(it) }")
        stringBuilder.append('\t').append('\t').appendLine("objectPool.putReceiverBuilder(${ksClassDeclaration.simpleName.asString()}::class.java) { ${ksClassDeclaration.receiverFileName}(it) }")
    }
    stringBuilder.append('\t').appendLine('}')
    stringBuilder.append('}')

    val collectorWriter = codeGenerator.createNewFile(
        dependencies = Dependencies(false),
        packageName = GeneratedPackageName,
        fileName = generatedClassName
    ).bufferedWriter()
    collectorWriter.write(stringBuilder.toString())
    collectorWriter.flush()
    collectorWriter.close()

    val resourceWriter = codeGenerator.createNewFileByPath(
        dependencies = Dependencies(false),
        path = "META-INF/services/${ObjectPool.Collector::class.java.name}",
        extensionName = ""
    ).bufferedWriter()
    resourceWriter.appendLine("${GeneratedPackageName}.${generatedClassName}")
    resourceWriter.flush()
    resourceWriter.close()
}
