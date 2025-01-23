package com.android.inter.process.compiler.ksp

import com.android.inter.process.compiler.utils.RandomHash
import com.android.inter.process.framework.ObjectPool
import com.android.inter.process.framework.annotation.IPCServiceFactory
import com.google.devtools.ksp.containingFile
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSType
import java.util.LinkedList

internal fun buildFunctionCollector(
    iPCInterfaceSymbols: List<KSAnnotated>,
    serviceFactorySymbols: List<KSAnnotated>,
    dependenciesFiles: List<KSFile>,
    resolver: Resolver,
    codeGenerator: CodeGenerator
): List<String> {
    if (iPCInterfaceSymbols.isEmpty() && serviceFactorySymbols.isEmpty()) return emptyList()
    val iPCInterfaceClassDeclarations = iPCInterfaceSymbols.findAnnotatedClassDeclarations()
    val serviceFactoryClassDeclarations = serviceFactorySymbols.findAnnotatedClassDeclarations()
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
            val interfaceClazz = annotation.arguments.find { it.name?.asString() == "interfaceClazz" }?.value
            if (interfaceClazz is KSType) {
                importAppend(requireNotNull(interfaceClazz.declaration.qualifiedName).asString())
            }
        }
    }

    val stringBuilder = StringBuilder()
    stringBuilder.appendLine(" // number of serviceFactorySymbols: ${serviceFactorySymbols.size}, number of serviceFactoryClassDeclarations: ${serviceFactoryClassDeclarations.size}")
    stringBuilder.appendLine(" // number of iPCInterfaceSymbols: ${iPCInterfaceSymbols.size}, number of iPCInterfaceClassDeclarations: ${iPCInterfaceClassDeclarations.size}")
    stringBuilder.appendLine("package $GeneratedPackageName")
    stringBuilder.appendLine()
    importList.forEach(stringBuilder::import)
    stringBuilder.appendLine()
    stringBuilder.appendLine("internal class $generatedClassName : ${ObjectPool.Collector::class.java.simpleName} {")
    stringBuilder.appendLine()
    stringBuilder.append('\t').appendLine("override fun collect() {")
    iPCInterfaceClassDeclarations.forEach { ksClassDeclaration ->
        stringBuilder.append('\t').append('\t').appendLine("objectPool.putCallerFactory(${ksClassDeclaration.simpleName.asString()}::class.java) { ${ksClassDeclaration.callerFileName}(it) }")
        stringBuilder.append('\t').append('\t').appendLine("objectPool.putReceiverFactory(${ksClassDeclaration.simpleName.asString()}::class.java) { ${ksClassDeclaration.receiverFileName}(it) }")
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
