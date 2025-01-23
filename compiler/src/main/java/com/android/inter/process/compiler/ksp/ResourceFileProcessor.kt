package com.android.inter.process.compiler.ksp

import com.android.inter.process.framework.ObjectPool
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSFile

fun resourceFileProcessor(
    collectorResourceList: List<String>,
    collectorSymbols: List<KSAnnotated>,
    dependenciesFiles: List<KSFile>,
    codeGenerator: CodeGenerator
) {
    if (collectorSymbols.isEmpty()) return

    val collectorSymbolNames = collectorSymbols.findAnnotatedClassDeclarations()
        .mapNotNull { it.qualifiedName?.asString() }
    val resourceWriter = codeGenerator.createNewFileByPath(
        dependencies = Dependencies(
            false,
            *dependenciesFiles.toTypedArray()
        ),
        path = "META-INF/services/${ObjectPool.Collector::class.java.name}",
        extensionName = ""
    ).bufferedWriter()
    (collectorResourceList + collectorSymbolNames).forEach { collectorName ->
        resourceWriter.appendLine(collectorName)
    }
    resourceWriter.flush()
    resourceWriter.close()
}