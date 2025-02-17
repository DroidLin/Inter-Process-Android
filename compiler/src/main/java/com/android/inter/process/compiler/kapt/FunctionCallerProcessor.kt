package com.android.inter.process.compiler.kapt

import com.android.inter.process.compiler.exceptions.IllegalDeclarationException
import com.android.inter.process.framework.FunctionCallAdapter
import com.android.inter.process.framework.annotation.IPCFunction
import com.android.inter.process.framework.annotation.IPCService
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.type.NoType

internal fun processCallerFunction(
    processingEnvironment: ProcessingEnvironment,
    environment: RoundEnvironment
) {
    val iPCServiceElements = environment.getElementsAnnotatedWith(IPCService::class.java)
        .filterIsInstance<TypeElement>()
    if (iPCServiceElements.isEmpty()) {
        return
    }
    val classStringList = iPCServiceElements.map(::createCallerForIPCServiceType)
    classStringList.forEachIndexed { index, classString ->
        val packageName = iPCServiceElements[index].packageName ?: return@forEachIndexed
        val className = iPCServiceElements[index].callerFileName
        processingEnvironment.filer.createSourceFile("${packageName}.${className}", *iPCServiceElements.toTypedArray())
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
    importOperation(Nullable::class.java.canonicalName)
    importOperation(Override::class.java.canonicalName)
    importOperation(FunctionCallAdapter::class.java.canonicalName)
    importOperation(typeElement.qualifiedName.toString())
    importOperation("com.android.inter.process.framework.FunctionCallAdapterKt")
    importOperation("com.android.inter.process.framework.JvmMethodRequestKt")
    importOperation("kotlin.collections.CollectionsKt")
    importOperation("com.android.inter.process.framework.metadata.AndroidBinderFunctionMetadataKt")

    typeElement.enclosedElements.forEach { element ->
        if (element is ExecutableElement) {
            val returnType = element.returnType
            if (returnType.isValidForFullName) {
                importOperation(returnType.fullName)
            }
            element.parameters.forEach { variableElement ->
                val variableType = variableElement.asType()
                if (variableType.isValidForFullName) {
                    importOperation(variableType.fullName)
                }
                variableElement.annotationMirrors.forEach { annotationMirror ->
                    if (annotationMirror.annotationType.isValidForFullName) {
                        importOperation(annotationMirror.annotationType.fullName)
                    }
                }
            }
            element.annotationMirrors.forEach { annotationMirror ->
                val annotationType = annotationMirror.annotationType
                importOperation(annotationType.fullName)
            }
        }
    }

    val stringBuilder = StringBuilder()
    stringBuilder
        .appendLine("package ${typeElement.packageName};")
        .appendLine()
        .apply {
            importLines.sort()
            importLines.forEach { importLine ->
                appendLine("import ${importLine};")
            }
        }
        .appendLine()
        .appendLine("public final class ${typeElement.callerFileName} implements ${typeElement.simpleName} {")
        .appendLine()
        .appendLine("\t@${NotNull::class.java.simpleName}")
        .appendLine("\tprivate final ${FunctionCallAdapter::class.java.simpleName} mTransformer;")
        .appendLine()
        .appendLine("\t@${Nullable::class.java.simpleName}")
        .appendLine("\tprivate final ${String::class.java.simpleName} mUniqueKey;")
        .appendLine()
        .appendLine("\tpublic ${typeElement.callerFileName}(@${NotNull::class.java.simpleName} ${FunctionCallAdapter::class.java.simpleName} mTransformer, @${Nullable::class.java.simpleName} ${String::class.java.simpleName} mUniqueKey) {")
        .appendLine("\t\tthis.mTransformer = mTransformer;")
        .appendLine("\t\tthis.mUniqueKey = mUniqueKey;")
        .appendLine("\t}")
        .appendLine()
        .apply {
            val memberElements = typeElement.enclosedElements
            for (element in memberElements) {
                when (element) {
                    is ExecutableElement -> {
                        if (element.typeParameters.isNotEmpty()) {
                            throw IllegalDeclarationException("type parameters declared in function ${element.simpleName} is not allowed!!!")
                        }
                        appendLine("\t// ${element.returnType}")
                        appendLine("\t// ${element.receiverType}")
                        appendLine("\t// ${element.annotationMirrors.joinToString { it.annotationType.toString() }}")
                        val parameters = element.parameters.joinToString { variableElement ->
                            appendLine("\t// ${variableElement.asType()}")
                            val variableAnnotations = variableElement.annotationMirrors.joinToString(separator = " ") { annotationMirror ->
                                "@${annotationMirror.annotationType.simpleName}"
                            }
                            if (variableAnnotations.isNotEmpty()) {
                                "$variableAnnotations ${variableElement.asType().simpleName} ${variableElement.simpleName}"
                            } else {
                                "${variableElement.asType().simpleName} ${variableElement.simpleName}"
                            }
                        }
                        // function annotations
                        element.annotationMirrors.forEach { annotationMirror ->
                            appendLine("\t@${annotationMirror.annotationType.simpleName}")
                        }
                        appendLine("\t@${Override::class.java.simpleName}")

                        val isReturnValueExist = element.returnType !is NoType
                        val isSuspendExecutable = element.isSuspendExecutable

                        // function body
                        appendLine("\tpublic final ${element.returnType.simpleName} ${element.simpleName}(${parameters}) {")
                        if (isSuspendExecutable) {
                            val parametersString = element.parameterNoContinuation.joinToString { variableElement ->
                                val iPCFunctionMirror = variableElement.annotationMirrors.find { it.annotationType.isValidForFullName && it.annotationType.simpleName == IPCFunction::class.java.simpleName }
                                if (iPCFunctionMirror != null) {
                                    "AndroidBinderFunctionMetadataKt.newBinderFunctionMetadata(${variableElement.asType().simpleName}.class, ${variableElement.simpleName})"
                                } else {
                                    variableElement.simpleName
                                }
                            }
                            appendLine("\t\t${if (isReturnValueExist) "return (${element.returnType.simpleName}) " else ""}this.mTransformer.call(")
                            appendLine("\t\t\tJvmMethodRequestKt.request(")
                            appendLine("\t\t\t\t${typeElement.simpleName}.class,")
                            appendLine("\t\t\t\tthis.mUniqueKey,")
                            appendLine("\t\t\t\t\"${element.identifier}\",")
                            appendLine("\t\t\t\tCollectionsKt.listOf(${parametersString}),")
                            appendLine("\t\t\t\t${isSuspendExecutable}")
                            appendLine("\t\t\t),")
                            appendLine("\t\t\t${requireNotNull(element.continuationVariable).simpleName}")
                            appendLine("\t\t);")
                        } else {
                            val parametersString = element.parameters.joinToString { variableElement ->
                                val iPCFunctionMirror = variableElement.annotationMirrors.find { it.annotationType.isValidForFullName && it.annotationType.simpleName == IPCFunction::class.java.simpleName }
                                if (iPCFunctionMirror != null) {
                                    "AndroidBinderFunctionMetadataKt.newBinderFunctionMetadata(${variableElement.asType().simpleName}.class, ${variableElement.simpleName})"
                                } else {
                                    variableElement.simpleName
                                }
                            }
                            appendLine("\t\t${if (isReturnValueExist) "return (${element.returnType.simpleName}) " else ""}FunctionCallAdapterKt.syncCall(")
                            appendLine("\t\t\tthis.mTransformer,")
                            appendLine("\t\t\tJvmMethodRequestKt.request(")
                            appendLine("\t\t\t\t${typeElement.simpleName}.class,")
                            appendLine("\t\t\t\tthis.mUniqueKey,")
                            appendLine("\t\t\t\t\"${element.identifier}\",")
                            appendLine("\t\t\t\tCollectionsKt.listOf(${parametersString}),")
                            appendLine("\t\t\t\t${isSuspendExecutable}")
                            appendLine("\t\t\t)")
                            appendLine("\t\t);")
                        }
                        appendLine("\t}")
                        appendLine()
                    }
                }
            }
        }
        .appendLine("}")

    return stringBuilder.toString()
}