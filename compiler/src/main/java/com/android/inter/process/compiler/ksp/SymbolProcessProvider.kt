package com.android.inter.process.compiler.ksp

import com.android.inter.process.framework.annotation.CustomCollector
import com.android.inter.process.framework.annotation.IPCService
import com.android.inter.process.framework.annotation.IPCServiceFactory
import com.google.devtools.ksp.containingFile
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
        val interfaceSymbols = resolver.getSymbolsWithAnnotation(IPCService::class.java.name).toList()
        buildCallerFunction(
            ksAnnotatedList = interfaceSymbols,
            resolver = resolver,
            codeGenerator = this.environment.codeGenerator
        )
        buildReceiverFunction(
            ksAnnotatedList = interfaceSymbols,
            resolver = resolver,
            codeGenerator = this.environment.codeGenerator
        )

        val serviceFactorySymbols = resolver.getSymbolsWithAnnotation(IPCServiceFactory::class.java.name).toList()
        val collectorResourceList = buildFunctionCollector(
            iPCInterfaceSymbols = interfaceSymbols,
            serviceFactorySymbols = serviceFactorySymbols,
            dependenciesFiles = (interfaceSymbols + serviceFactorySymbols).mapNotNull { it.containingFile },
            resolver = resolver,
            codeGenerator = this.environment.codeGenerator
        )

        val customCollectorSymbols = resolver.getSymbolsWithAnnotation(CustomCollector::class.java.name).toList()
        resourceFileProcessor(
            collectorResourceList = collectorResourceList,
            collectorSymbols = customCollectorSymbols,
            dependenciesFiles = (customCollectorSymbols + serviceFactorySymbols).mapNotNull { it.containingFile },
            codeGenerator = this.environment.codeGenerator
        )
        return emptyList()
    }
}