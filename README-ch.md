# Inter Process Android
这是一个面向接口开发的，基于Android系统的跨进程通信工具，如果你也有使用AIDL调用远端函数的功能，但是觉得这个开发过程很繁琐，那么希望这个工具可以帮到你。

# 写在前面
这个工具本身是基于Android Binder通讯，具体原理就不细展开了，借助binder驱动进行更高级的上层封装来进行更快捷的跨进程调用、传输数据；

架构上参考了Retrofit的动态代理的设计，基于接口化开发，将原来面向AIDL接口的开发过程简化到了Kotlin、Java的接口开发，由开发者自己提供接口与实现，通过注解自动扫描并注册到中心容器，开发者只需要从中心容器获取接口，即可快速实现优雅的跨进程调用。

本工具的核心是将通用的Binder对象传给远端进程，这样就实现了单向跨进程调用；**举例来说**，我在A进程构造了一个Binder对象传给了B进程，那么我就可以实现B -> A的方法调用，B进程就可以调用A的方法了；同样的，将A进程的Binder对象传递给B，就能实现A <--> B的双向调用。

# 如何安装
目前还在开发阶段，整体功能已经测试完毕但还未进行发布，会尽快完成aar发布。

# 如何使用
- 前置声明你的辅助类，可以快速方便的访问你的远端接口代理对象.
```kotlin
object ProcessManager {
    private val context: Context get() = App.context
    private val providers = ConcurrentHashMap<Address, IPCProvider>()

    /**
     * 表示你希望从哪个进程获取接口实现，换句话讲就是你想调用哪个进程的方法；
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
- 然后在 ```AndroidManifest.xml``` 添加Android的Provider组件:

Provider组件主要用于A进程需要连接B进程时，当B进程未启动，则自动拉起B进程，相关唤起逻辑由系统服务ActivityManagerService统一调度；

**注意**：别忘了给Provider标签补充进程名和唯一标识，这是能够成功访问到远端进程的核心。
```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <application>
        <provider
            android:authorities="@string/content_provider_main_ipc"
            android:name=".Provider0"
            android:exported="false" />
        <provider
            android:authorities="@string/content_provider_lib_ipc"
            android:name=".Provider1"
            android:process=":broadcast"
            android:exported="false" />
        <provider
            android:authorities="@string/content_provider_provider_ipc"
            android:name=".Provider2"
            android:process=":provider"
            android:exported="false" />
    </application>
</manifest>
```
- 声明Provider的实现类，不同进程提供不同实现:
```kotlin
// class ProcessComponent
class Provider0 : ContentAndroidProvider()

class Provider1 : ContentAndroidProvider()

class Provider2 : ContentAndroidProvider()
```
- 定义统一的Provider唯一标识：
```xml
<?xml version="1.0" encoding="utf-8" ?>
<resources>
    <string name="content_provider_main_ipc">com.android.inter.process.provider.main</string>
    <string name="content_provider_lib_ipc">com.android.inter.process.provider.broadcast</string>
    <string name="content_provider_provider_ipc">com.android.inter.process.provider.provider</string>
</resources>
```

- 在进程启动时对工具进行初始化

**注意**：这个仅仅是启动时的模板代码，你的代码可以提供更深层次的封装；
```kotlin
class App : Application() {

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        context = this

        val packageName = getProcessName(this)
        println("process name: ${packageName}.")
        if (packageName.endsWith(":broadcast")) { // broadcast process
            IPCManager.installAndroid(InitConfig(ProcessManager.ProcessAddress.broadcast))
        } else if (packageName.endsWith(":provider")) { // provider process
            IPCManager.installAndroid(InitConfig(ProcessManager.ProcessAddress.provider))
        } else if (!packageName.contains(":")) { // main process
            IPCManager.installAndroid(InitConfig(ProcessManager.ProcessAddress.main))
        }
    }

    companion object {

        fun getProcessName(context: Context): String {
            val pid = Process.myPid()
            return kotlin.runCatching {
                (context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager)
                    ?.runningAppProcesses?.find { it.pid == pid }?.processName
            }.getOrNull() ?: ""
        }
    }
}
```

- 定义开发者自己的业务接口

就像前面提到的那样，像基本数据类型、Serializable接口的实现类、Parcelable接口的实现类这些类的实例化对象都能够通过生命接口参数的形式通过这个工具进行跨进程传输；

简而言之，AIDL里能传输的对象，本工具也能传输；
```kotlin
data class Metadata(val data: Long, val string: String): Seriazliable

// 注解定义通过kapt、ksp compiler编译期生成辅助代码，提高运行时性能
@IPCService
interface MyInterface : INoProguard {

    fun getMetadata(): Metadata

    suspend fun getMetadataSuspend(): Metadata
}
```

- 将ksp或者kapt的依赖添加到你的module中.
```kotlin
dependencies {
    // wait for further release publish.
    <!-- kapt("xxx:xxx:1.0.0") -->
    <!-- ksp("xxx:xxx:1.0.0") -->
}
```

- 声明你的接口实现
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

- 对你的接口实现声明工厂构造，并使用注解装饰

编译期会自动收集这些工厂，运行时自动注册
```kotlin
@IPCServiceFactory(interfaceClazz = MyInterface::class, uniqueKey = "")
class MyInterfaceServiceFactory : ServiceFactory<MyInterface> {
    override fun serviceCreate(): MyInterfae {
        return MyInterfaceService()
    }
}
```

- 在你的代码中使用
```kotlin
fun main() {
    val myInterface = ProcessManager.fromPlayerProcess(MyInterface::class.java)
    val metadata: Metadata? = kotlin.runCatching {
        myInterface.getMetadata()
    }.getOrNull()
}
```
- 对你定义的接口提供不同的实现类

为了充分满足同一个接口有多个实现的场景，工厂对象在注册是提供了唯一key：`uniqueKey`，装饰工厂类时可补充到注解上
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
    // 决定希望调用哪个进程的方法，就从对应实现里获取代理类，像调用一个本地接口一样使用就可以了
    val myInterfaceA = ProcessManager.fromBroadcastProcess(MyInterface::class.java, KEY_INTERFACE_A)
    val myInterfaceB = ProcessManager.fromBroadcastProcess(MyInterface::class.java, KEY_INTERFACE_B)
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
