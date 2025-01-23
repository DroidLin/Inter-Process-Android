package com.android.inter.process.test

import android.os.ParcelFileDescriptor
import com.android.inter.process.framework.ServiceFactory
import com.android.inter.process.framework.annotation.IPCServiceFactory
import com.android.inter.process.test.metadata.ParcelableMetadata
import com.android.inter.process.test.metadata.SerializableMetadata
import java.io.FileInputStream

interface InterfaceReflectionService {

    val versionNumber: Int
    val versionName: String
    val serializableMetadata: SerializableMetadata
    val parcelableMetadata: ParcelableMetadata
    val String.numberOfChar: Int
    val ParcelFileDescriptor.content: String

    fun versionNumber(): Int
    fun versionName(): String
    fun serializableMetadata(): SerializableMetadata
    fun parcelableMetadata(): ParcelableMetadata
    fun String.numberOfChar(): Int
    fun ParcelFileDescriptor.content(): String

    fun noReturnValue()
    fun noReturnValueOneParameter(number: Int)
    fun noReturnValueTwoParameter(number: Int, str: String)
    fun noReturnValueThreeParameter(number: Int, str: String, serializableMetadata: SerializableMetadata)
    fun noReturnValueFourParameter(number: Int, str: String, serializableMetadata: SerializableMetadata, parcelableMetadata: ParcelableMetadata)
}

@IPCServiceFactory(interfaceClazz = InterfaceReflectionService::class)
class InterfaceReflectionServiceFactory : ServiceFactory<InterfaceReflectionService> {
    override fun serviceCreate(): InterfaceReflectionService {
        return InterfaceReflectionServiceImplementation()
    }
}

class InterfaceReflectionServiceImplementation : InterfaceReflectionService {

    override val versionNumber: Int get() = 100
    override val versionName: String get() = "100"
    override val serializableMetadata: SerializableMetadata
        get() = SerializableMetadata.Default
    override val parcelableMetadata: ParcelableMetadata
        get() = ParcelableMetadata.Default
    override val String.numberOfChar: Int get() = length
    override val ParcelFileDescriptor.content: String
        get() = use { FileInputStream(it.fileDescriptor).bufferedReader().readText() }

    override fun versionNumber(): Int {
        return 100
    }

    override fun versionName(): String {
        return "100"
    }

    override fun serializableMetadata(): SerializableMetadata {
        return SerializableMetadata.Default
    }

    override fun parcelableMetadata(): ParcelableMetadata {
        return ParcelableMetadata.Default
    }

    override fun String.numberOfChar(): Int {
        return length
    }

    override fun ParcelFileDescriptor.content(): String {
        return use { FileInputStream(it.fileDescriptor).bufferedReader().readText() }
    }

    override fun noReturnValue() {
        println("noReturnValue.")
    }

    override fun noReturnValueOneParameter(number: Int) {
        println("noReturnValue: number: ${number}.")
    }

    override fun noReturnValueTwoParameter(number: Int, str: String) {
        println("noReturnValue: number: ${number}, str: ${str}.")
    }

    override fun noReturnValueThreeParameter(number: Int, str: String, serializableMetadata: SerializableMetadata) {
        println("noReturnValue: number: ${number}, str: ${str}.")
    }

    override fun noReturnValueFourParameter(number: Int, str: String, serializableMetadata: SerializableMetadata, parcelableMetadata: ParcelableMetadata) {
        println("noReturnValue: number: ${number}, str: ${str}, serializableMetadata: ${serializableMetadata}, parcelableMetadata: ${parcelableMetadata}.")
    }
}