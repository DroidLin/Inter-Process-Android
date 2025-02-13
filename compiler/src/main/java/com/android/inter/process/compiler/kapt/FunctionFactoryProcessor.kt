package com.android.inter.process.compiler.kapt

import com.android.inter.process.compiler.utils.CollectorClassName
import com.android.inter.process.compiler.utils.GeneratedPackageName
import com.android.inter.process.framework.ObjectPool
import com.android.inter.process.framework.annotation.IPCService
import com.android.inter.process.framework.annotation.IPCServiceFactory
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror

internal fun processCustomFunctionCollector(
    processingEnvironment: ProcessingEnvironment,
    environment: RoundEnvironment
) {
    val iPCServiceElements = environment.getElementsAnnotatedWith(IPCService::class.java)
        .filterIsInstance<TypeElement>()
    val iPCServiceFactoryElements =
        environment.getElementsAnnotatedWith(IPCServiceFactory::class.java)
            .filterIsInstance<TypeElement>()

    if (iPCServiceElements.isEmpty() && iPCServiceFactoryElements.isEmpty()) {
        return
    }

    val importLines = mutableListOf<String>()
    val importOperation = { importLine: String ->
        if (!importLines.contains(importLine)) {
            importLines += importLine
        }
    }

    importOperation(ObjectPool.Collector::class.java.canonicalName)
    importOperation(Override::class.java.canonicalName)
    importOperation(ObjectPool::class.java.canonicalName)
    importOperation("com.android.inter.process.framework.ObjectPoolKt")

    iPCServiceElements.forEach { typeElement ->
        importOperation(typeElement.qualifiedName.toString())
        importOperation("${typeElement.packageName}.${typeElement.callerFileName}")
        importOperation("${typeElement.packageName}.${typeElement.receiverFileName}")
    }
    iPCServiceFactoryElements.forEach { typeElement ->
        importOperation(typeElement.qualifiedName.toString())
        val annotation =
            typeElement.annotationMirrors.find { it.annotationType.toString() == IPCServiceFactory::class.java.name }
        annotation?.elementValues?.forEach { (executableElement, annotationValue) ->
            if (executableElement.simpleName.toString() == IPCServiceFactory::interfaceClazz.name) {
                val type = annotationValue.value
                if (type is TypeMirror && type.isValidForFullName) {
                    importOperation(type.fullName)
                }
            }
        }
    }

    val stringBuilder = StringBuilder()
    stringBuilder
        .appendLine("package ${GeneratedPackageName};")
        .appendLine()
        .apply {
            importLines.sort()
            importLines.forEach { importLine ->
                appendLine("import ${importLine};")
            }
        }
        .appendLine()
        .appendLine("public final class $CollectorClassName implements ${ObjectPool.Collector::class.java.simpleName} {")
        .appendLine("\t@${Override::class.java.simpleName}")
        .appendLine("\tpublic void collect() {")
        .apply {
            iPCServiceElements.forEach { typeElement ->
                appendLine("\t\tObjectPoolKt.getObjectPool().putCallerFactory(${typeElement.simpleName}.class, (adapter) -> { return new ${typeElement.callerFileName}(adapter); });")
                appendLine("\t\tObjectPoolKt.getObjectPool().putReceiverFactory(${typeElement.simpleName}.class, (mInstance) -> { return new ${typeElement.receiverFileName}(mInstance); });")
            }
            iPCServiceFactoryElements.forEach { typeElement ->
                val annotation =
                    typeElement.annotationMirrors.find { it.annotationType.toString() == IPCServiceFactory::class.java.name }
                annotation?.elementValues?.forEach { (executableElement, annotationValue) ->
                    if (executableElement.simpleName.toString() == IPCServiceFactory::interfaceClazz.name) {
                        val type = annotationValue.value
                        if (type is TypeMirror) {
                            appendLine("\t\tObjectPoolKt.getObjectPool().putInstanceFactory(${type.simpleName}.class, new ${typeElement.simpleName}());")
                        }
                    }
                }
            }
        }
        .appendLine("\t}")
        .appendLine("}")

    val javaFileObject = processingEnvironment.filer.createSourceFile(
        "${GeneratedPackageName}.${CollectorClassName}",
        *(iPCServiceElements + iPCServiceFactoryElements).toTypedArray()
    )
    try {
        javaFileObject.openWriter()
            .use { it.write(stringBuilder.toString()); it.flush() }
    } catch (throwable: Throwable) {
        throwable.printStackTrace()
        javaFileObject.delete()
    }
}