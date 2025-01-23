package com.android.inter.process

import android.content.Context
import android.os.ParcelFileDescriptor
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.android.inter.process.framework.Address
import com.android.inter.process.framework.IPCProvider
import com.android.inter.process.framework.address.broadcast
import com.android.inter.process.test.InterfaceReflectionService
import com.android.inter.process.test.metadata.ParcelableMetadata
import com.android.inter.process.test.metadata.SerializableMetadata
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class InterfaceReflectTest {

    private var interfaceServiceCache: InterfaceReflectionService? = null

    val appContext: Context
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    val remoteProcessAddress: Address
        get() {
            return broadcast(
                context = appContext,
                broadcastAction = appContext.getString(R.string.broadcast_action_lib_ipc)
            )
        }

    val interfaceService: InterfaceReflectionService
        get() {
            if (this.interfaceServiceCache == null) {
                this.interfaceServiceCache = IPCProvider.on(remoteProcessAddress)
                    .serviceCreate(InterfaceReflectionService::class.java)
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
        val fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        try {
            assert(tmpContent == interfaceService.run { fileDescriptor.content })
        } finally {
            fileDescriptor.close()
        }
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
        try {
            assert(tmpContent == interfaceService.run { fileDescriptor.content() })
        } finally {
            fileDescriptor.close()
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
}