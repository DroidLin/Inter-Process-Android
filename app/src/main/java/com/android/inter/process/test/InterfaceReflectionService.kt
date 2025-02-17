package com.android.inter.process.test

import android.os.ParcelFileDescriptor
import com.android.inter.process.framework.ServiceFactory
import com.android.inter.process.framework.annotation.IPCFunction
import com.android.inter.process.framework.annotation.IPCServiceFactory
import com.android.inter.process.framework.annotation.NoProguard
import com.android.inter.process.test.metadata.ParcelableMetadata
import com.android.inter.process.test.metadata.SerializableMetadata
import java.io.FileInputStream

@NoProguard
interface InterfaceReflectionService {

    val versionNumber: Int
    val versionName: String
    val serializableMetadata: SerializableMetadata
    val parcelableMetadata: ParcelableMetadata
    val String.numberOfChar: Int
    val ParcelFileDescriptor.content: String

    var mutableVersionNumber: Int
    var mutableVersionName: String
    var mutableSerializableMetadata: SerializableMetadata
    var mutableParcelableMetadata: ParcelableMetadata

    var String.mutableNumberOfChar: Int
    var ParcelFileDescriptor.mutableContent: String

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

    fun noReturnNoValueCallback(@IPCFunction callback: () -> Unit)
    fun noReturnOneValueCallback(@IPCFunction callback: (number: Int) -> Unit)
    fun noReturnTwoValueCallback(@IPCFunction callback: (number: Int, str: String) -> Unit)
    fun noReturnThreeValueCallback(@IPCFunction callback: (number: Int, str: String, serializableMetadata: SerializableMetadata) -> Unit)
    fun noReturnFourValueCallback(@IPCFunction callback: (number: Int, str: String, serializableMetadata: SerializableMetadata, parcelableMetadata: ParcelableMetadata) -> Unit)

    suspend fun suspendVersionNumber(): Int
    suspend fun suspendVersionName(): String
    suspend fun suspendSerializableMetadata(): SerializableMetadata
    suspend fun suspendParcelableMetadata(): ParcelableMetadata
    suspend fun String.suspendNumberOfChar(): Int
    suspend fun ParcelFileDescriptor.suspendContent(): String

    suspend fun suspendNoReturnValue()
    suspend fun suspendNoReturnValueOneParameter(number: Int)
    suspend fun suspendNoReturnValueTwoParameter(number: Int, str: String)
    suspend fun suspendNoReturnValueThreeParameter(number: Int, str: String, serializableMetadata: SerializableMetadata)
    suspend fun suspendNoReturnValueFourParameter(number: Int, str: String, serializableMetadata: SerializableMetadata, parcelableMetadata: ParcelableMetadata)

    suspend fun suspendNoReturnNoValueCallback(@IPCFunction callback: () -> Unit)
    suspend fun suspendNoReturnOneValueCallback(@IPCFunction callback: (number: Int) -> Unit)
    suspend fun suspendNoReturnTwoValueCallback(@IPCFunction callback: (number: Int, str: String) -> Unit)
    suspend fun suspendNoReturnThreeValueCallback(@IPCFunction callback: (number: Int, str: String, serializableMetadata: SerializableMetadata) -> Unit)
    suspend fun suspendNoReturnFourValueCallback(@IPCFunction callback: (number: Int, str: String, serializableMetadata: SerializableMetadata, parcelableMetadata: ParcelableMetadata) -> Unit)
}

@IPCServiceFactory(interfaceClazz = InterfaceReflectionService::class, uniqueKey = "HelloWorld!")
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

    override var mutableVersionNumber: Int = 0
    override var mutableVersionName: String = ""
    override var mutableSerializableMetadata: SerializableMetadata = SerializableMetadata.Default
    override var mutableParcelableMetadata: ParcelableMetadata = ParcelableMetadata.Default

    override var String.mutableNumberOfChar: Int
        get() = 0
        set(value) {}
    override var ParcelFileDescriptor.mutableContent: String
        get() = ""
        set(value) {}

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

    override fun noReturnNoValueCallback(callback: () -> Unit) {
        callback()
    }

    override fun noReturnOneValueCallback(callback: (number: Int) -> Unit) {
        callback(100)
    }

    override fun noReturnTwoValueCallback(callback: (number: Int, str: String) -> Unit) {
        callback(100, "hello world!")
    }

    override fun noReturnThreeValueCallback(callback: (number: Int, str: String, serializableMetadata: SerializableMetadata) -> Unit) {
        callback(100, "hello world!", SerializableMetadata("hahahha", 10232L, 100))
    }

    override fun noReturnFourValueCallback(callback: (number: Int, str: String, serializableMetadata: SerializableMetadata, parcelableMetadata: ParcelableMetadata) -> Unit) {
        callback(100, "hello world!", SerializableMetadata("hahahha", 10232L, 100), ParcelableMetadata(210, "android"))
    }

    override suspend fun suspendVersionNumber(): Int {
        return 100
    }

    override suspend fun suspendVersionName(): String {
        return "100"
    }

    override suspend fun suspendSerializableMetadata(): SerializableMetadata {
        return SerializableMetadata.Default
    }

    override suspend fun suspendParcelableMetadata(): ParcelableMetadata {
        return ParcelableMetadata.Default
    }

    override suspend fun String.suspendNumberOfChar(): Int {
        return length
    }

    override suspend fun ParcelFileDescriptor.suspendContent(): String {
        return use { FileInputStream(it.fileDescriptor).bufferedReader().readText() }
    }

    override suspend fun suspendNoReturnValue() {
        println("suspendNoReturnValue.")
    }

    override suspend fun suspendNoReturnValueOneParameter(number: Int) {
        println("suspendNoReturnValueOneParameter: number: ${number}.")
    }

    override suspend fun suspendNoReturnValueTwoParameter(number: Int, str: String) {
        println("suspendNoReturnValueTwoParameter: number: ${number}, str: ${str}.")
    }

    override suspend fun suspendNoReturnValueThreeParameter(
        number: Int,
        str: String,
        serializableMetadata: SerializableMetadata
    ) {
        println("suspendNoReturnValueThreeParameter: number: ${number}, str: ${str}.")
    }

    override suspend fun suspendNoReturnValueFourParameter(
        number: Int,
        str: String,
        serializableMetadata: SerializableMetadata,
        parcelableMetadata: ParcelableMetadata
    ) {
        println("suspendNoReturnValueFourParameter: number: ${number}, str: ${str}, serializableMetadata: ${serializableMetadata}, parcelableMetadata: ${parcelableMetadata}.")
    }

    override suspend fun suspendNoReturnNoValueCallback(callback: () -> Unit) {
        callback()
    }

    override suspend fun suspendNoReturnOneValueCallback(callback: (number: Int) -> Unit) {
        callback(100)
    }

    override suspend fun suspendNoReturnTwoValueCallback(callback: (number: Int, str: String) -> Unit) {
        callback(100, "hello world!")
    }

    override suspend fun suspendNoReturnThreeValueCallback(callback: (number: Int, str: String, serializableMetadata: SerializableMetadata) -> Unit) {
        callback(100, "hello world!", SerializableMetadata("hahahha", 10232L, 100))
    }

    override suspend fun suspendNoReturnFourValueCallback(callback: (number: Int, str: String, serializableMetadata: SerializableMetadata, parcelableMetadata: ParcelableMetadata) -> Unit) {
        callback(100, "hello world!", SerializableMetadata("hahahha", 10232L, 100), ParcelableMetadata(210, "android"))
    }
}