package com.android.inter.process.compiler.kapt

import com.android.inter.process.compiler.utils.CollectorClassName
import com.android.inter.process.compiler.utils.GeneratedPackageName
import com.android.inter.process.framework.ObjectPool
import com.android.inter.process.framework.annotation.CustomCollector
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.TypeElement
import javax.tools.StandardLocation

internal fun processResource(
    processingEnvironment: ProcessingEnvironment,
    environment: RoundEnvironment
) {
    val customCollectElements = environment.getElementsAnnotatedWith(CustomCollector::class.java)
        .filterIsInstance<TypeElement>()
    if (customCollectElements.isEmpty()) {
        return
    }

    val stringBuilder = StringBuilder()
    stringBuilder.appendLine("${GeneratedPackageName}.${CollectorClassName}")
    customCollectElements.forEach { typeElement ->
        if (typeElement.asType().isValidForFullName) {
            stringBuilder.appendLine(typeElement.asType().fullName)
        }
    }

    processingEnvironment.filer.createResource(StandardLocation.CLASS_OUTPUT, "", "META-INF/services/${ObjectPool.Collector::class.java.name}")
        .openWriter()
        .use { it.write(stringBuilder.toString()); it.flush() }
}