package com.android.inter.process.compiler.services

import com.android.inter.process.compiler.kapt.startProcessor
import com.android.inter.process.framework.annotation.CustomCollector
import com.android.inter.process.framework.annotation.IPCFunction
import com.android.inter.process.framework.annotation.IPCService
import com.android.inter.process.framework.annotation.IPCServiceFactory
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement

class KotlinAnnotationProcessor : AbstractProcessor() {

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf(
            IPCService::class.java.canonicalName,
            IPCServiceFactory::class.java.canonicalName,
            IPCFunction::class.java.canonicalName,
            CustomCollector::class.java.canonicalName,
        )
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latestSupported()
    }

    override fun process(p0: MutableSet<out TypeElement>?, p1: RoundEnvironment): Boolean {
        startProcessor(this.processingEnv, p1)
        return true
    }
}