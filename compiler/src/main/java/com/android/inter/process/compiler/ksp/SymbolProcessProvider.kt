package com.android.inter.process.compiler.ksp

import com.android.inter.process.framework.annotation.IPCInterface
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated

class KspFunctionProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return Processor(environment)
    }
}

private class Processor(private val environment: SymbolProcessorEnvironment) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation(IPCInterface::class.java.name).toList()
        buildCallerFunction(symbols, resolver, this.environment.codeGenerator)
        buildReceiverFunction(symbols, resolver, this.environment.codeGenerator)
        buildFunctionCollector(symbols, resolver, this.environment.codeGenerator)
        return emptyList()
    }
}