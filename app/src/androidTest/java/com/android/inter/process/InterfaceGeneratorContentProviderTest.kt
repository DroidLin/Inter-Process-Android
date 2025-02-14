package com.android.inter.process

import android.content.Context
import android.os.ParcelFileDescriptor
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.android.inter.process.framework.Address
import com.android.inter.process.framework.IPCProvider
import com.android.inter.process.framework.address.provider
import com.android.inter.process.test.InterfaceGeneratorService
import com.android.inter.process.test.metadata.ParcelableMetadata
import com.android.inter.process.test.metadata.SerializableMetadata
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class InterfaceGeneratorContentProviderTest {

    private var interfaceServiceCache: InterfaceGeneratorService? = null

    private val appContext: Context
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    private val remoteProcessAddress: Address
        get() {
            return provider(
                context = appContext,
                authorities = appContext.getString(R.string.content_provider_lib_ipc),
            )
        }

    private val interfaceService: InterfaceGeneratorService
        get() {
            if (this.interfaceServiceCache == null) {
                this.interfaceServiceCache = IPCProvider.on(remoteProcessAddress)
                    .serviceCreate(InterfaceGeneratorService::class.java)
            }
            return requireNotNull(this.interfaceServiceCache)
        }

    @Test
    fun numberPropertyTest() {
        assert(interfaceService.versionNumber == 100)
    }

    @Test
    fun stringPropertyTest() {
        assert(interfaceService.versionName == "100")
    }

    @Test
    fun serializableMetadataPropertyTest() {
        assert(interfaceService.serializableMetadata == SerializableMetadata.Default)
    }

    @Test
    fun parcelableMetadataPropertyTest() {
        assert(interfaceService.parcelableMetadata == ParcelableMetadata.Default)
    }

    @Test
    fun extensionStringNumberPropertyTest() {
        val declaredString = "Hello World"
        assert(interfaceService.run { declaredString.numberOfChar } == declaredString.length)
    }

    @Test
    fun fileDescriptorPropertyTest() {
        val tmpContent = System.currentTimeMillis().toString() + Math.random()
        val file = File(appContext.cacheDir, "tmp.txt")
        file.bufferedWriter().use { it.write(tmpContent); it.flush() }
        ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY).use { fileDescriptor ->
            assert(tmpContent == interfaceService.run { fileDescriptor.content })
        }
    }

    @Test
    fun mutableNumberTest() {
        this.interfaceService.mutableVersionNumber = 100
        Assert.assertEquals(this.interfaceService.mutableVersionNumber, 100)
    }

    @Test
    fun mutableStringTest() {
        this.interfaceService.mutableVersionName = "100"
        Assert.assertEquals(this.interfaceService.mutableVersionName, "100")
    }

    @Test
    fun mutableSerializableMetadataTest() {
        val serializableMetadata = SerializableMetadata("100", 1000L, 12)
        this.interfaceService.mutableSerializableMetadata = serializableMetadata
        Assert.assertEquals(this.interfaceService.mutableSerializableMetadata, serializableMetadata)
    }

    @Test
    fun mutableParcelableMetadataTest() {
        val parcelableMetadata = ParcelableMetadata(100, "123")
        this.interfaceService.mutableParcelableMetadata = parcelableMetadata
        Assert.assertEquals(this.interfaceService.mutableParcelableMetadata, parcelableMetadata)
    }

    @Test
    fun numberFunctionTest() {
        assert(interfaceService.versionNumber() == 100)
    }

    @Test
    fun stringFunctionTest() {
        assert(interfaceService.versionName() == "100")
    }

    @Test
    fun serializableMetadataFunctionTest() {
        assert(interfaceService.serializableMetadata() == SerializableMetadata.Default)
    }

    @Test
    fun parcelableMetadataFunctionTest() {
        assert(interfaceService.parcelableMetadata() == ParcelableMetadata.Default)
    }

    @Test
    fun extensionStringNumberFunctionTest() {
        val declaredString = "Hello World"
        assert(interfaceService.run { declaredString.numberOfChar() } == declaredString.length)
    }

    @Test
    fun fileDescriptorFunctionTest() {
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
        interfaceService.noReturnValue()
    }

    @Test
    fun noReturnValueOneParameter() {
        interfaceService.noReturnValueOneParameter(100)
    }

    @Test
    fun noReturnValueTwoParameter() {
        interfaceService.noReturnValueTwoParameter(100, "hello world!")
    }

    @Test
    fun noReturnValueThreeParameter() {
        interfaceService.noReturnValueThreeParameter(
            number = 100,
            str = "hello world!",
            serializableMetadata = SerializableMetadata("hahahha", 10232L, 100)
        )
    }

    @Test
    fun noReturnValueFourParameter() {
        interfaceService.noReturnValueFourParameter(
            number = 100,
            str = "hello world!",
            serializableMetadata = SerializableMetadata("hahahha", 10232L, 100),
            parcelableMetadata = ParcelableMetadata(210, "android")
        )
    }

    @Test
    fun noReturnNoValueCallback() {
        interfaceService.noReturnNoValueCallback {
            println("noReturnNoValueCallback")
        }
    }

    @Test
    fun noReturnOneValueCallback() {
        interfaceService.noReturnOneValueCallback { number ->
            println("noReturnNoValueCallback, number: ${number}.")
        }
    }

    @Test
    fun noReturnTwoValueCallback() {
        interfaceService.noReturnTwoValueCallback { number, str ->
            println("noReturnNoValueCallback, number: ${number}, str: ${str}.")
        }
    }

    @Test
    fun noReturnThreeValueCallback() {
        interfaceService.noReturnThreeValueCallback { number, str, serializableMetadata ->
            println("noReturnNoValueCallback, number: ${number}, str: ${str}, serializableMetadata: ${serializableMetadata}.")
        }
    }

    @Test
    fun noReturnFourValueCallback() {
        interfaceService.noReturnFourValueCallback { number, str, serializableMetadata, parcelableMetadata ->
            println("noReturnNoValueCallback, number: ${number}, str: ${str}, serializableMetadata: ${serializableMetadata}, parcelableMetadata: ${parcelableMetadata}.")
        }
    }

    @Test
    fun suspendNumberFunctionTest() {
        runBlocking {
            assert(interfaceService.suspendVersionNumber() == 100)
        }
    }

    @Test
    fun suspendStringFunctionTest() {
        runBlocking {
            assert(interfaceService.suspendVersionName() == "100")
        }
    }

    @Test
    fun suspendSerializableMetadataFunctionTest() {
        runBlocking {
            assert(interfaceService.suspendSerializableMetadata() == SerializableMetadata.Default)
        }
    }

    @Test
    fun suspendParcelableMetadataFunctionTest() {
        runBlocking {
            assert(interfaceService.suspendParcelableMetadata() == ParcelableMetadata.Default)
        }
    }

    @Test
    fun suspendExtensionStringNumberFunctionTest() {
        runBlocking {
            val declaredString = "Hello World"
            assert(interfaceService.run { declaredString.suspendNumberOfChar() } == declaredString.length)
        }
    }

    @Test
    fun suspendFileDescriptorFunctionTest() {
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
        runBlocking {
            interfaceService.suspendNoReturnValue()
        }
    }

    @Test
    fun suspendNoReturnValueOneParameter() {
        runBlocking {
            interfaceService.suspendNoReturnValueOneParameter(100)
        }
    }

    @Test
    fun suspendNoReturnValueTwoParameter() {
        runBlocking {
            interfaceService.suspendNoReturnValueTwoParameter(100, "hello world!")
        }
    }

    @Test
    fun suspendNoReturnValueThreeParameter() {
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
        runBlocking {
            interfaceService.suspendNoReturnNoValueCallback {
                println("noReturnNoValueCallback")
            }
        }
    }

    @Test
    fun suspendNoReturnOneValueCallback() {
        runBlocking {
            interfaceService.suspendNoReturnOneValueCallback { number ->
                println("noReturnNoValueCallback, number: ${number}.")
            }
        }
    }

    @Test
    fun suspendNoReturnTwoValueCallback() {
        runBlocking {
            interfaceService.suspendNoReturnTwoValueCallback { number, str ->
                println("noReturnNoValueCallback, number: ${number}, str: ${str}.")
            }
        }
    }

    @Test
    fun suspendNoReturnThreeValueCallback() {
        runBlocking {
            interfaceService.suspendNoReturnThreeValueCallback { number, str, serializableMetadata ->
                println("noReturnNoValueCallback, number: ${number}, str: ${str}, serializableMetadata: ${serializableMetadata}.")
            }
        }
    }

    @Test
    fun suspendNoReturnFourValueCallback() {
        runBlocking {
            interfaceService.suspendNoReturnFourValueCallback { number, str, serializableMetadata, parcelableMetadata ->
                println("noReturnNoValueCallback, number: ${number}, str: ${str}, serializableMetadata: ${serializableMetadata}, parcelableMetadata: ${parcelableMetadata}.")
            }
        }
    }
}