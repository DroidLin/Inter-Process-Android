package com.android.inter.process

import android.content.Intent
import android.os.Bundle
import android.os.ParcelFileDescriptor
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.android.inter.process.framework.IPCProvider
import com.android.inter.process.framework.address.broadcast
import com.android.inter.process.framework.address.provider
import com.android.inter.process.ui.theme.InterProcessAndroidTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * @author: liuzhongao
 * @since: 2024/9/17 18:58
 */
class LibActivity : ComponentActivity() {

    private val iPCProvider by lazy {
        val address = provider(this, getString(R.string.content_provider_main_ipc))
        IPCProvider.on(address)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val applicationInfo = iPCProvider.serviceCreate(ApplicationInfo::class.java)
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
                                    emptyCallbackFunction {
                                        println("emptyCallbackFunction")
                                    }
                                }
                            }
                        ) {
                            Text(text = "emptyCallbackFunction")
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
                        Button(onClick = {
                            val intent = Intent(this@LibActivity, MainActivity::class.java)
                            startActivity(intent)
                        }) {
                            Text(text = "Go MainActivity")
                        }
                    }
                }
            }
        }
    }
}