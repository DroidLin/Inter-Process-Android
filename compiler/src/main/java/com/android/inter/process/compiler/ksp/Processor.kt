package com.android.inter.process.compiler.ksp

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment

internal fun startSymbolProcessor(resolver: Resolver, environment: SymbolProcessorEnvironment) {
    processCallerFunction(resolver = resolver, codeGenerator = environment.codeGenerator)
    processReceiverFunction(resolver = resolver, codeGenerator = environment.codeGenerator)
    resourceFileProcessor(
        collectorResourceList = processCustomFunctionCollector(
            resolver = resolver,
            codeGenerator = environment.codeGenerator
        ),
        resolver = resolver,
        codeGenerator = environment.codeGenerator
    )
}