package com.android.inter.process.compiler.ksp

import com.android.inter.process.framework.ObjectPool
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies

fun resourceFileProcessor(
    collectorSymbols: List<String>,
    codeGenerator: CodeGenerator
) {
    if (collectorSymbols.isEmpty()) return
    val resourceWriter = codeGenerator.createNewFileByPath(
        dependencies = Dependencies(false),
        path = "META-INF/services/${ObjectPool.Collector::class.java.name}",
        extensionName = ""
    ).bufferedWriter()
    collectorSymbols.forEach { collectorName ->
        resourceWriter.appendLine(collectorName)
    }
    resourceWriter.flush()
    resourceWriter.close()
}