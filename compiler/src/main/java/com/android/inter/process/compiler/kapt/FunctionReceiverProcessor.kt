package com.android.inter.process.compiler.kapt

import com.android.inter.process.framework.annotation.IPCService
import com.android.inter.process.framework.reflect.InvocationParameter
import com.android.inter.process.framework.reflect.InvocationReceiver
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.type.NoType
import kotlin.coroutines.Continuation

fun processReceiverFunction(
    processingEnvironment: ProcessingEnvironment,
    environment: RoundEnvironment
) {
    val iPCServiceElements = environment.getElementsAnnotatedWith(IPCService::class.java)
        .filterIsInstance<TypeElement>()
    if (iPCServiceElements.isEmpty()) {
        return
    }
    val classStringList = iPCServiceElements.map(::createReceiverForIPCServiceType)
    classStringList.forEachIndexed { index, classString ->
        val packageName = iPCServiceElements[index].packageName ?: return@forEachIndexed
        val className = iPCServiceElements[index].receiverFileName
        processingEnvironment.filer.createSourceFile("${packageName}.${className}", *iPCServiceElements.toTypedArray())
            .openWriter()
            .use { it.write(classString); it.flush() }
    }
}

private fun createReceiverForIPCServiceType(typeElement: TypeElement): String {
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
    importOperation(List::class.java.canonicalName)
    importOperation(InvocationReceiver::class.java.canonicalName)
    importOperation(InvocationParameter::class.java.canonicalName)
    importOperation(typeElement.qualifiedName.toString())
    importOperation(Continuation::class.java.canonicalName)

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
        .appendLine("public final class ${typeElement.receiverFileName} implements ${InvocationReceiver::class.java.simpleName}<${typeElement.simpleName}> {")
        .appendLine()
        .appendLine("\t@${NotNull::class.java.simpleName}")
        .appendLine("\tprivate final ${typeElement.simpleName} mInstance;")
        .appendLine()
        .appendLine("\tpublic ${typeElement.receiverFileName}(@${NotNull::class.java.simpleName} ${typeElement.simpleName} mInstance) {")
        .appendLine("\t\tthis.mInstance = mInstance;")
        .appendLine("\t}")
        .appendLine()
    stringBuilder
        .appendLine("\t@${Nullable::class.java.simpleName}")
        .appendLine("\t@${Override::class.java.simpleName}")
        .appendLine("\tpublic ${Object::class.java.simpleName} invoke(@${NotNull::class.java.simpleName} ${InvocationParameter::class.java.simpleName} invocationParameter) {")
        .apply {
            val memberElements = typeElement.nonSuspendEnclosedElements
            if (memberElements.isNotEmpty()) {
                appendLine("\t\t${Object::class.java.simpleName} response = null;")
                    .appendLine("\t\t${List::class.java.simpleName}<${Object::class.java.simpleName}> params = invocationParameter.getMethodParameterValues();")
                    .appendLine("\t\tswitch(invocationParameter.getUniqueKey()) {")
                    .apply {
                        for (element in memberElements) {
                            val isReturnValueExist = element.returnType !is NoType
                            val parameters = StringBuilder()
                            element.parameters.forEachIndexed { index, variableElement ->
                                if (index != 0) {
                                    parameters.append(", ")
                                }
                                parameters.append("(${variableElement.asType().simpleName}) params.get(${index})")
                            }
                            appendLine("\t\t\tcase \"${element.identifier}\":")
                            if (isReturnValueExist) {
                                appendLine("\t\t\t\tresponse = this.mInstance.${element.simpleName}(${parameters});")
                            } else {
                                appendLine("\t\t\t\tthis.mInstance.${element.simpleName}(${parameters});")
                            }
                            appendLine("\t\t\t\tbreak;")
                        }
                    }
                    .appendLine("\t\t}")
                    .appendLine("\t\treturn response;")
            } else {
                appendLine("\t\treturn null;")
            }
        }
        .appendLine("\t}")

    stringBuilder.appendLine()
        .appendLine("\t@${Nullable::class.java.simpleName}")
        .appendLine("\t@${Override::class.java.simpleName}")
        .appendLine("\tpublic ${Object::class.java.simpleName} invokeSuspend(@${NotNull::class.java.simpleName} ${InvocationParameter::class.java.simpleName} invocationParameter, @${NotNull::class.java.simpleName} ${Continuation::class.java.simpleName}<? super Object> \$completion) {")
        .apply {
            val memberElements = typeElement.suspendEnclosedElements
            if (memberElements.isNotEmpty()) {
                appendLine("\t\t${Object::class.java.simpleName} response = null;")
                    .appendLine("\t\t${List::class.java.simpleName}<${Object::class.java.simpleName}> params = invocationParameter.getMethodParameterValues();")
                    .appendLine("\t\tswitch(invocationParameter.getUniqueKey()) {")
                    .apply {
                        for (element in memberElements) {
                            val isReturnValueExist = element.returnType !is NoType
                            val parameters = StringBuilder()
                            element.parameterNoContinuation.forEachIndexed { index, variableElement ->
                                if (index != 0) {
                                    parameters.append(", ")
                                }
                                parameters.append("(${variableElement.asType().simpleName}) params.get(${index})")
                            }
                            appendLine("\t\t\tcase \"${element.identifier}\":")
                            if (isReturnValueExist) {
                                if (parameters.isNotEmpty()) {
                                    appendLine("\t\t\t\tresponse = this.mInstance.${element.simpleName}(${parameters}, \$completion);")
                                } else {
                                    appendLine("\t\t\t\tresponse = this.mInstance.${element.simpleName}(\$completion);")
                                }
                            } else {
                                if (parameters.isNotEmpty()) {
                                    appendLine("\t\t\t\tthis.mInstance.${element.simpleName}(${parameters}, \$completion);")
                                } else {
                                    appendLine("\t\t\t\tthis.mInstance.${element.simpleName}(\$completion);")
                                }
                            }
                            appendLine("\t\t\t\tbreak;")
                        }
                    }
                    .appendLine("\t\t}")
                    .appendLine("\t\treturn response;")
            } else {
                appendLine("\t\treturn null;")
            }
        }
        .appendLine("\t}")
    stringBuilder.appendLine("}")
    return stringBuilder.toString()
}