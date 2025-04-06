package com.android.inter.process.compiler.ksp

import com.android.inter.process.compiler.utils.GeneratedPackageName
import com.android.inter.process.compiler.utils.RandomHash
import com.android.inter.process.framework.ObjectPool
import com.android.inter.process.framework.annotation.IPCService
import com.android.inter.process.framework.annotation.IPCServiceFactory
import com.google.devtools.ksp.containingFile
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSType
import java.util.LinkedList

internal fun processCustomFunctionCollector(
    resolver: Resolver,
    codeGenerator: CodeGenerator
): List<String> {
    val iPCInterfaceClassDeclarations = resolver.getSymbolsWithAnnotation(IPCService::class.java.name)
        .toList()
        .findAnnotatedClassDeclarations()
    val serviceFactoryClassDeclarations = resolver.getSymbolsWithAnnotation(IPCServiceFactory::class.java.name)
        .toList()
        .findAnnotatedClassDeclarations()
    val dependenciesFiles = (iPCInterfaceClassDeclarations + serviceFactoryClassDeclarations).mapNotNull { it.containingFile }
    if (iPCInterfaceClassDeclarations.isEmpty() && serviceFactoryClassDeclarations.isEmpty()) return emptyList()

    val generatedClassName = "${ObjectPool.Collector::class.java.simpleName}_${RandomHash}"
    val importList = LinkedList<String>()
    val importAppend = { value: String ->
        if (!importList.contains(value)) {
            importList += value
        }
    }

    iPCInterfaceClassDeclarations.forEach { classDeclaration ->
        importAppend(requireNotNull(classDeclaration.qualifiedName).asString())
        importAppend(classDeclaration.callerClassName)
        importAppend(classDeclaration.receiverClassName)
    }
    importAppend("com.android.inter.process.framework.objectPool")
    importAppend("com.android.inter.process.framework.ObjectPool.Collector")
    serviceFactoryClassDeclarations.forEach { classDeclaration ->
        importAppend(requireNotNull(classDeclaration.qualifiedName).asString())
        val annotation = classDeclaration.annotations.find { it.shortName.asString() == IPCServiceFactory::class.java.simpleName }
        if (annotation != null) {
            val interfaceClazz = annotation.arguments.find { it.name?.asString() == IPCServiceFactory::interfaceClazz.name }?.value
            if (interfaceClazz is KSType) {
                importAppend(requireNotNull(interfaceClazz.declaration.qualifiedName).asString())
            }
        }
    }

    val stringBuilder = StringBuilder()
    stringBuilder.appendLine("package $GeneratedPackageName")
    stringBuilder.appendLine()
    importList.forEach(stringBuilder::import)
    stringBuilder.appendLine()
    stringBuilder.appendLine("internal class $generatedClassName : ${ObjectPool.Collector::class.java.simpleName} {")
    stringBuilder.appendLine()
    stringBuilder.append('\t').appendLine("override fun collect() {")
    iPCInterfaceClassDeclarations.forEach { ksClassDeclaration ->
        stringBuilder.append('\t').append('\t').appendLine("objectPool.putCallerFactory(${ksClassDeclaration.simpleName.asString()}::class.java) { adapter, uniqueKey -> ${ksClassDeclaration.callerFileName}(adapter, uniqueKey) }")
        stringBuilder.append('\t').append('\t').appendLine("objectPool.putReceiverFactory(${ksClassDeclaration.simpleName.asString()}::class.java) { instance -> ${ksClassDeclaration.receiverFileName}(instance) }")
    }
    serviceFactoryClassDeclarations.forEach { ksClassDeclaration ->
        val annotation = ksClassDeclaration.annotations.find { it.shortName.asString() == IPCServiceFactory::class.java.simpleName }
        if (annotation != null) {
            val interfaceClazz = annotation.arguments.find { it.name?.asString() == "interfaceClazz" }?.value
            if (interfaceClazz is KSType) {
                stringBuilder.append('\t').append('\t').appendLine("objectPool.putInstanceFactory(${interfaceClazz.declaration.simpleName.asString()}::class.java, ${ksClassDeclaration.simpleName.asString()}())")
            }
        }
    }
    stringBuilder.append('\t').appendLine('}')
    stringBuilder.append('}')

    val collectorWriter = codeGenerator.createNewFile(
        dependencies = Dependencies(false, *dependenciesFiles.toTypedArray()),
        packageName = GeneratedPackageName,
        fileName = generatedClassName
    ).bufferedWriter()
    collectorWriter.write(stringBuilder.toString())
    collectorWriter.flush()
    collectorWriter.close()

    return listOf("${GeneratedPackageName}.${generatedClassName}")
}
