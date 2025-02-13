package com.android.inter.process.compiler.kapt

import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment

internal fun startProcessor(processingEnvironment: ProcessingEnvironment, environment: RoundEnvironment) {
    processCallerFunction(processingEnvironment, environment)
}