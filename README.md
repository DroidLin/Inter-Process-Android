# Inter Process Android
this is a toolkit target android platform communicate to remote process, through interface-developing communication. if you got a situation to take ipc call to another process, but find aidl is rather hard to use, this toolkit might be useful for you.

# How to install
sorry the repository does not provide compiled packages yet, we will publish latest releases as soon as possible.

# How to use
- declare your process installations.
```kotlin

object ProcessManager {

    private val context: Context get() = App.context

    private val providers = ConcurrentHashMap<Address, IPCProvider>()

    /**
     * this means you want to get implementations towards main process,
     * the following functions are the same.
     */
    @JvmOverloads
    fun <T> fromMainProcess(clazz: Class<T>, uniqueKey: String? = null): T {
        return this.getProviderInstance(ProcessAddress.main)
            .serviceCreate(clazz, uniqueKey)
    }

    @JvmOverloads
    fun <T> fromBroadcastProcess(clazz: Class<T>, uniqueKey: String? = null): T {
        return this.getProviderInstance(ProcessAddress.broadcast)
            .serviceCreate(clazz, uniqueKey)
    }

    @JvmOverloads
    fun <T> fromProviderProcess(clazz: Class<T>, uniqueKey: String? = null): T {
        return this.getProviderInstance(ProcessAddress.provider)
            .serviceCreate(clazz, uniqueKey)
    }

    private fun getProviderInstance(address: Address): IPCProvider {
        return this.providers.getOrPut(address) { IPCProvider.on(address) }
    }

    object ProcessAddress {

        val main: Address
            get() = provider(context, context.getString(R.string.content_provider_main_ipc))

        val broadcast: Address
            get() = provider(context, context.getString(R.string.content_provider_lib_ipc))

        val provider: Address
            get() = provider(context, context.getString(R.string.content_provider_provider_ipc))
    }
}
```
- declare your own interface
```kotlin
data class Metadata(val data: Long, val string: String): Seriazliable

// kapt or ksp tool will generate helper code to improve running performance.
@IPCService
interface MyInterface : INoProguard {

    fun getMetadata(): Metadata
    
    suspend fun getMetadataSuspend(): Metadata
}
```
- add ksp or kapt dependencies to your module.
```kotlin
dependencies {
    // wait for further release publish.
    implementation("xxx:xxx:1.0.0")
}
```
- declare your interface implementations
```kotlin
class MyInterfaceService : MyInterface {

    override fun getMetadata(): Metadata {
        return Metadata(1L, "")
    }

    override suspend fun getMetadataSuspend(): Metadata {
        return Metadata(1L, "")
    }
}
```
- declare implementation collector
```kotlin
@IPCServiceFactory(interfaceClazz = MyInterface::class, uniqueKey = "")
class MyInterfaceServiceFactory : ServiceFactory<MyInterface> {
    override fun serviceCreate(): MyInterfae {
        return MyInterfaceService()
    }
}
```
- use in your code
```kotlin
fun main() {
    val myInterface = ProcessManager.fromPlayerProcess(MyInterface::class.java)
    val metadata: Metadata? = kotlin.runCatching {
        myInterface.getMetadata()
    }.getOrNull()
}
```
- define different implementation of one interface
```kotlin
const val KEY_INTERFACE_A = "key_a"
const val KEY_INTERFACE_B = "key_b"

@IPCServiceFactory(interfaceClazz = MyInterface::class, uniqueKey = KEY_INTERFACE_A)
class MyInterfaceServiceFactory : ServiceFactory<MyInterface> {
    override fun serviceCreate(): MyInterfae {
        // provide different implementation here.
        return MyInterfaceServiceA()
    }
}

@IPCServiceFactory(interfaceClazz = MyInterface::class, uniqueKey = KEY_INTERFACE_B)
class MyInterfaceServiceFactory : ServiceFactory<MyInterface> {
    override fun serviceCreate(): MyInterfae {
        // provide different implementation here.
        return MyInterfaceServiceB()
    }
}

fun main() {
    val myInterfaceA = ProcessManager.fromPlayerProcess(MyInterface::class.java, KEY_INTERFACE_A)
    val myInterfaceB = ProcessManager.fromPlayerProcess(MyInterface::class.java, KEY_INTERFACE_B)
    // do your jobs.
}
```

# LICENSE
MIT License

Copyright (c) 202503 liuzhongao

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
