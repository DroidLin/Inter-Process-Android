package com.android.inter.process

import android.content.Context
import android.os.ParcelFileDescriptor
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.android.inter.process.test.InterfaceGeneratorService
import com.android.inter.process.test.InterfaceGeneratorServiceImplementation
import com.android.inter.process.test.metadata.ParcelableMetadata
import com.android.inter.process.test.metadata.SerializableMetadata
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/**
 * test local implementation for current process.
 */
@RunWith(AndroidJUnit4::class)
class InterfaceLocalTest {

    private var interfaceServiceCache: InterfaceGeneratorService? = null

    private val appContext: Context
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    private val interfaceService: InterfaceGeneratorService
        get() {
            if (this.interfaceServiceCache == null) {
                this.interfaceServiceCache = ProcessManager.fromMainProcess(InterfaceGeneratorService::class.java)
            }
            return requireNotNull(this.interfaceServiceCache)
        }

    @Test
    fun numberPropertyTest() {
        assert(interfaceService is InterfaceGeneratorServiceImplementation)
        assert(interfaceService.versionNumber == 100)
    }

    @Test
    fun stringPropertyTest() {
        assert(interfaceService is InterfaceGeneratorServiceImplementation)
        assert(interfaceService.versionName == "100")
    }

    @Test
    fun serializableMetadataPropertyTest() {
        assert(interfaceService is InterfaceGeneratorServiceImplementation)
        assert(interfaceService.serializableMetadata == SerializableMetadata.Default)
    }

    @Test
    fun parcelableMetadataPropertyTest() {
        assert(interfaceService is InterfaceGeneratorServiceImplementation)
        assert(interfaceService.parcelableMetadata == ParcelableMetadata.Default)
    }

    @Test
    fun extensionStringNumberPropertyTest() {
        assert(interfaceService is InterfaceGeneratorServiceImplementation)
        val declaredString = "Hello World"
        assert(interfaceService.run { declaredString.numberOfChar } == declaredString.length)
    }

    @Test
    fun fileDescriptorPropertyTest() {
        assert(interfaceService is InterfaceGeneratorServiceImplementation)
        val tmpContent = System.currentTimeMillis().toString() + Math.random()
        val file = File(appContext.cacheDir, "tmp.txt")
        file.bufferedWriter().use { it.write(tmpContent); it.flush() }
        ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY).use { fileDescriptor ->
            assert(tmpContent == interfaceService.run { fileDescriptor.content })
        }
    }

    @Test
    fun mutableNumberTest() {
        assert(interfaceService is InterfaceGeneratorServiceImplementation)
        this.interfaceService.mutableVersionNumber = 100
        Assert.assertEquals(this.interfaceService.mutableVersionNumber, 100)
    }

    @Test
    fun mutableStringTest() {
        assert(interfaceService is InterfaceGeneratorServiceImplementation)
        this.interfaceService.mutableVersionName = "100"
        Assert.assertEquals(this.interfaceService.mutableVersionName, "100")
    }

    @Test
    fun mutableSerializableMetadataTest() {
        assert(interfaceService is InterfaceGeneratorServiceImplementation)
        val serializableMetadata = SerializableMetadata("100", 1000L, 12)
        this.interfaceService.mutableSerializableMetadata = serializableMetadata
        Assert.assertEquals(this.interfaceService.mutableSerializableMetadata, serializableMetadata)
    }

    @Test
    fun mutableParcelableMetadataTest() {
        assert(interfaceService is InterfaceGeneratorServiceImplementation)
        val parcelableMetadata = ParcelableMetadata(100, "123")
        this.interfaceService.mutableParcelableMetadata = parcelableMetadata
        Assert.assertEquals(this.interfaceService.mutableParcelableMetadata, parcelableMetadata)
    }

    @Test
    fun numberFunctionTest() {
        assert(interfaceService is InterfaceGeneratorServiceImplementation)
        assert(interfaceService.versionNumber() == 100)
    }

    @Test
    fun stringFunctionTest() {
        assert(interfaceService is InterfaceGeneratorServiceImplementation)
        assert(interfaceService.versionName() == "100")
    }

    @Test
    fun serializableMetadataFunctionTest() {
        assert(interfaceService is InterfaceGeneratorServiceImplementation)
        assert(interfaceService.serializableMetadata() == SerializableMetadata.Default)
    }

    @Test
    fun parcelableMetadataFunctionTest() {
        assert(interfaceService is InterfaceGeneratorServiceImplementation)
        assert(interfaceService.parcelableMetadata() == ParcelableMetadata.Default)
    }

    @Test
    fun extensionStringNumberFunctionTest() {
        assert(interfaceService is InterfaceGeneratorServiceImplementation)
        val declaredString = "Hello World"
        assert(interfaceService.run { declaredString.numberOfChar() } == declaredString.length)
    }

    @Test
    fun fileDescriptorFunctionTest() {
        assert(interfaceService is InterfaceGeneratorServiceImplementation)
        val tmpContent = System.currentTimeMillis().toString() + Math.random()
        val file = File(appContext.cacheDir, "tmp.txt")
        file.bufferedWriter().use { it.write(tmpContent); it.flush() }
        val fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        fileDescriptor.use { descriptor ->
            assert(tmpContent == interfaceService.run { descriptor.content() })
        }
    }

    @Test
    fun noReturnFunctionTest() {
        assert(interfaceService is InterfaceGeneratorServiceImplementation)
        interfaceService.noReturnValue()
    }

    @Test
    fun noReturnValueOneParameter() {
        assert(interfaceService is InterfaceGeneratorServiceImplementation)
        interfaceService.noReturnValueOneParameter(100)
    }

    @Test
    fun noReturnValueTwoParameter() {
        assert(interfaceService is InterfaceGeneratorServiceImplementation)
        interfaceService.noReturnValueTwoParameter(100, "hello world!")
    }

    @Test
    fun noReturnValueThreeParameter() {
        assert(interfaceService is InterfaceGeneratorServiceImplementation)
        interfaceService.noReturnValueThreeParameter(
            number = 100,
            str = "hello world!",
            serializableMetadata = SerializableMetadata("hahahha", 10232L, 100)
        )
    }

    @Test
    fun noReturnValueFourParameter() {
        assert(interfaceService is InterfaceGeneratorServiceImplementation)
        interfaceService.noReturnValueFourParameter(
            number = 100,
            str = "hello world!",
            serializableMetadata = SerializableMetadata("hahahha", 10232L, 100),
            parcelableMetadata = ParcelableMetadata(210, "android")
        )
    }

    @Test
    fun noReturnNoValueCallback() {
        assert(interfaceService is InterfaceGeneratorServiceImplementation)
        interfaceService.noReturnNoValueCallback {
            println("noReturnNoValueCallback")
        }
    }

    @Test
    fun noReturnOneValueCallback() {
        assert(interfaceService is InterfaceGeneratorServiceImplementation)
        interfaceService.noReturnOneValueCallback { number ->
            println("noReturnNoValueCallback, number: ${number}.")
        }
    }

    @Test
    fun noReturnTwoValueCallback() {
        assert(interfaceService is InterfaceGeneratorServiceImplementation)
        interfaceService.noReturnTwoValueCallback { number, str ->
            println("noReturnNoValueCallback, number: ${number}, str: ${str}.")
        }
    }

    @Test
    fun noReturnThreeValueCallback() {
        assert(interfaceService is InterfaceGeneratorServiceImplementation)
        interfaceService.noReturnThreeValueCallback { number, str, serializableMetadata ->
            println("noReturnNoValueCallback, number: ${number}, str: ${str}, serializableMetadata: ${serializableMetadata}.")
        }
    }

    @Test
    fun noReturnFourValueCallback() {
        assert(interfaceService is InterfaceGeneratorServiceImplementation)
        interfaceService.noReturnFourValueCallback { number, str, serializableMetadata, parcelableMetadata ->
            println("noReturnNoValueCallback, number: ${number}, str: ${str}, serializableMetadata: ${serializableMetadata}, parcelableMetadata: ${parcelableMetadata}.")
        }
    }

    @Test
    fun suspendNumberFunctionTest() {
        assert(interfaceService is InterfaceGeneratorServiceImplementation)
        runBlocking {
            assert(interfaceService.suspendVersionNumber() == 100)
        }
    }

    @Test
    fun suspendStringFunctionTest() {
        assert(interfaceService is InterfaceGeneratorServiceImplementation)
        runBlocking {
            assert(interfaceService.suspendVersionName() == "100")
        }
    }

    @Test
    fun suspendSerializableMetadataFunctionTest() {
        assert(interfaceService is InterfaceGeneratorServiceImplementation)
        runBlocking {
            assert(interfaceService.suspendSerializableMetadata() == SerializableMetadata.Default)
        }
    }

    @Test
    fun suspendParcelableMetadataFunctionTest() {
        assert(interfaceService is InterfaceGeneratorServiceImplementation)
        runBlocking {
            assert(interfaceService.suspendParcelableMetadata() == ParcelableMetadata.Default)
        }
    }

    @Test
    fun suspendExtensionStringNumberFunctionTest() {
        assert(interfaceService is InterfaceGeneratorServiceImplementation)
        runBlocking {
            val declaredString = "Hello World"
            assert(interfaceService.run { declaredString.suspendNumberOfChar() } == declaredString.length)
        }
    }

    @Test
    fun suspendFileDescriptorFunctionTest() {
        assert(interfaceService is InterfaceGeneratorServiceImplementation)
        runBlocking {
            val tmpContent = System.currentTimeMillis().toString() + Math.random()
            val file = File(appContext.cacheDir, "tmp.txt")
            file.bufferedWriter().use { it.write(tmpContent); it.flush() }
            ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY).use { fileDescriptor ->
                assert(tmpContent == interfaceService.run { fileDescriptor.suspendContent() })
            }
        }
    }

    @Test
    fun suspendNoReturnFunctionTest() {
        assert(interfaceService is InterfaceGeneratorServiceImplementation)
        runBlocking {
            interfaceService.suspendNoReturnValue()
        }
    }

    @Test
    fun suspendNoReturnValueOneParameter() {
        assert(interfaceService is InterfaceGeneratorServiceImplementation)
        runBlocking {
            interfaceService.suspendNoReturnValueOneParameter(100)
        }
    }

    @Test
    fun suspendNoReturnValueTwoParameter() {
        assert(interfaceService is InterfaceGeneratorServiceImplementation)
        runBlocking {
            interfaceService.suspendNoReturnValueTwoParameter(100, "hello world!")
        }
    }

    @Test
    fun suspendNoReturnValueThreeParameter() {
        assert(interfaceService is InterfaceGeneratorServiceImplementation)
        runBlocking {
            interfaceService.suspendNoReturnValueThreeParameter(
                number = 100,
                str = "hello world!",
                serializableMetadata = SerializableMetadata("hahahha", 10232L, 100)
            )
        }
    }

    @Test
    fun suspendNoReturnValueFourParameter() {
        assert(interfaceService is InterfaceGeneratorServiceImplementation)
        runBlocking {
            interfaceService.suspendNoReturnValueFourParameter(
                number = 100,
                str = "hello world!",
                serializableMetadata = SerializableMetadata("hahahha", 10232L, 100),
                parcelableMetadata = ParcelableMetadata(210, "android")
            )
        }
    }

    @Test
    fun suspendNoReturnNoValueCallback() {
        assert(interfaceService is InterfaceGeneratorServiceImplementation)
        runBlocking {
            interfaceService.suspendNoReturnNoValueCallback {
                println("noReturnNoValueCallback")
            }
        }
    }

    @Test
    fun suspendNoReturnOneValueCallback() {
        assert(interfaceService is InterfaceGeneratorServiceImplementation)
        runBlocking {
            interfaceService.suspendNoReturnOneValueCallback { number ->
                println("noReturnNoValueCallback, number: ${number}.")
            }
        }
    }

    @Test
    fun suspendNoReturnTwoValueCallback() {
        assert(interfaceService is InterfaceGeneratorServiceImplementation)
        runBlocking {
            interfaceService.suspendNoReturnTwoValueCallback { number, str ->
                println("noReturnNoValueCallback, number: ${number}, str: ${str}.")
            }
        }
    }

    @Test
    fun suspendNoReturnThreeValueCallback() {
        assert(interfaceService is InterfaceGeneratorServiceImplementation)
        runBlocking {
            interfaceService.suspendNoReturnThreeValueCallback { number, str, serializableMetadata ->
                println("noReturnNoValueCallback, number: ${number}, str: ${str}, serializableMetadata: ${serializableMetadata}.")
            }
        }
    }

    @Test
    fun suspendNoReturnFourValueCallback() {
        assert(interfaceService is InterfaceGeneratorServiceImplementation)
        runBlocking {
            interfaceService.suspendNoReturnFourValueCallback { number, str, serializableMetadata, parcelableMetadata ->
                println("noReturnNoValueCallback, number: ${number}, str: ${str}, serializableMetadata: ${serializableMetadata}, parcelableMetadata: ${parcelableMetadata}.")
            }
        }
    }
}