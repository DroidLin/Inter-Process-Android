package com.android.inter.process

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.ParcelFileDescriptor
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.android.inter.process.framework.IPCProvider
import com.android.inter.process.framework.address.broadcast
import com.android.inter.process.framework.address.provider
import com.android.inter.process.test.metadata.ParcelableMetadata
import com.android.inter.process.test.metadata.SerializableMetadata
import com.android.inter.process.ui.theme.InterProcessAndroidTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val applicationInfo = ProcessManager.fromBroadcastProcess(ApplicationInfo::class.java)
        setContent {
            InterProcessAndroidTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val coroutineScope = rememberCoroutineScope()
                        val startLauncher: ApplicationInfo.(suspend ApplicationInfo.() -> Unit) -> Unit = { suspendFunction ->
                            coroutineScope.launch {
                                suspendFunction()
                            }
                        }
                        TextButton(
                            onClick = {
                                applicationInfo.startLauncher { packageName }
                            }
                        ) {
                            Text(text = "packageName")
                        }
                        TextButton(
                            onClick = {
                                applicationInfo.startLauncher { callRemote("hello world", listOf(0, 0, 0, 0)) }
                            }
                        ) {
                            Text(text = "callRemote")
                        }
                        TextButton(
                            onClick = {
                                applicationInfo.startLauncher {
                                    suspendNoReturnNoValueCallback {
                                        println("suspendNoReturnNoValueCallback")
                                    }
                                }
                            }
                        ) {
                            Text(text = "suspendNoReturnNoValueCallback")
                        }
                        TextButton(
                            onClick = {
                                applicationInfo.startLauncher {
                                    withContext(Dispatchers.IO) {
                                        val file = File(cacheDir, "tmp.txt").also { file ->
                                            file.bufferedWriter().use { writer ->
                                                writer.write("hello world write data.")
                                                writer.flush()
                                            }
                                        }
                                        ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY).use { descriptor ->
                                            writeData(descriptor)
                                        }
                                    }
                                }
                            }
                        ) {
                            Text(text = "Parcel fileDescriptor writeData")
                        }
                        TextButton(
                            onClick = {
                                applicationInfo.startLauncher {
                                    val serializableMetadata = SerializableMetadata("100", 1000L, 12)
                                    mutableSerializableMetadata = serializableMetadata
                                    println("equals = ${serializableMetadata == mutableSerializableMetadata}.")
                                }
                            }
                        ) {
                            Text(text = "property setter")
                        }
                        TextButton(
                            onClick = {
                                applicationInfo.startLauncher {
                                    withContext(Dispatchers.IO) {
                                        val data = getData("")
                                        println("data = ${data}.")
                                    }
                                }
                            }
                        ) {
                            Text(text = "getData")
                        }
                        Button(onClick = {
                            val intent = Intent(this@MainActivity, LibActivity::class.java)
                            startActivity(intent)
                        }) {
                            Text(text = "Go LibActivity")
                        }
                    }
                }
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            this.window.decorView.viewTreeObserver.addOnWindowVisibilityChangeListener {

            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    InterProcessAndroidTheme {
        Greeting("Android")
    }
}

fun onClick(activity: ComponentActivity, applicationInfo: ApplicationInfo) {
    applicationInfo.isRight()
}
