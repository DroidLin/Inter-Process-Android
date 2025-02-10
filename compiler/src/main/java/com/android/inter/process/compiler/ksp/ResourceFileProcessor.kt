package com.android.inter.process.compiler.ksp

import com.android.inter.process.framework.ObjectPool
import com.android.inter.process.framework.annotation.CustomCollector
import com.android.inter.process.framework.annotation.IPCServiceFactory
import com.google.devtools.ksp.containingFile
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSFile

fun resourceFileProcessor(
    collectorResourceList: List<String>,
    resolver: Resolver,
    codeGenerator: CodeGenerator
) {
    val customKSAnnotated = resolver.getSymbolsWithAnnotation(CustomCollector::class.java.name).toList()
    val collectorSymbolNames = customKSAnnotated
        .findAnnotatedClassDeclarations()
        .mapNotNull { it.qualifiedName?.asString() }
    val serviceFactorySymbols = resolver.getSymbolsWithAnnotation(IPCServiceFactory::class.java.name).toList()
    val dependenciesFiles = (customKSAnnotated + serviceFactorySymbols).mapNotNull { it.containingFile }

    if (collectorSymbolNames.isEmpty()) return
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