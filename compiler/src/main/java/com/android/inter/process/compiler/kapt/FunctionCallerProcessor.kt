package com.android.inter.process.compiler.kapt

import com.android.inter.process.framework.FunctionCallAdapter
import com.android.inter.process.framework.annotation.IPCService
import org.jetbrains.annotations.NotNull
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement

internal fun processCallerFunction(
    processingEnvironment: ProcessingEnvironment,
    environment: RoundEnvironment
) {
    val iPCServiceElements = environment.getElementsAnnotatedWith(IPCService::class.java)
        .filterIsInstance<TypeElement>()

    val classStringList = iPCServiceElements.map(::createCallerForIPCServiceType)
    classStringList.forEachIndexed { index, classString ->
        val packageName = iPCServiceElements[index].packageName ?: return@forEachIndexed
        val className = iPCServiceElements[index].callerFileName
        processingEnvironment.filer.createSourceFile("${packageName}.${className}")
            .openWriter()
            .use { it.write(classString); it.flush() }
    }
}

private fun createCallerForIPCServiceType(typeElement: TypeElement): String {
    require(typeElement.kind == ElementKind.INTERFACE)

    val importLines = mutableListOf<String>()
    val importOperation = { importLine: String ->
        if (!importLines.contains(importLine)) {
            importLines += importLine
        }
    }

    importOperation(NotNull::class.java.canonicalName)
    importOperation(Override::class.java.canonicalName)
    importOperation(FunctionCallAdapter::class.java.canonicalName)
    importOperation(typeElement.qualifiedName.toString())

    typeElement.enclosedElements.forEach { element ->
        if (element is ExecutableElement) {
            importOperation(element.returnType.fullName)
            element.parameters.forEach { variableElement ->
                importOperation(variableElement.asType().fullName)
            }
        }
    }

    val stringBuilder = StringBuilder()
    stringBuilder
        .appendLine("package ${typeElement.packageName};")
        .appendLine()
        .apply {
            importLines.forEach { importLine ->
                appendLine("import ${importLine};")
            }
        }
        .appendLine()
        .appendLine("public final class ${typeElement.callerFileName} implements ${typeElement.simpleName} {")
        .appendLine()
        .appendLine("\t@${NotNull::class.java.simpleName}")
        .appendLine("\tpublic final ${FunctionCallAdapter::class.java.simpleName} mTransformer;")
        .appendLine()
        .appendLine("\tpublic ${typeElement.callerFileName}(@${NotNull::class.java.simpleName} ${FunctionCallAdapter::class.java.simpleName} mTransformer) {")
        .appendLine("\t\tthis.mTransformer = mTransformer;")
        .appendLine("\t}")
        .appendLine()
        .apply {
            val memberElements = typeElement.enclosedElements
            for (element in memberElements) {
                when (element) {
                    is ExecutableElement -> {
                        appendLine("\t@${Override::class.java.simpleName}")
                            .appendLine("\tpublic final ${element.returnType.simpleName} ${element.simpleName}(${element.parameters.joinToString { "${it.asType().simpleName} ${it.simpleName}" } }) {")
                            .appendLine("\t}")
                            .appendLine()
                    }
                }
            }
        }
        .appendLine("}")

    return stringBuilder.toString()
}