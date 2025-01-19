package com.android.inter.process

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.android.inter.process.framework.IPCProvider
import com.android.inter.process.framework.address.broadcast
import com.android.inter.process.ui.theme.InterProcessAndroidTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val iPCProvider by lazy {
        IPCProvider.on(
            broadcast(
                context = this,
                broadcastAction = getString(R.string.broadcast_action_lib_ipc)
            )
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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

                        Text(
                            text = "call process name",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                .clickable(
                                    interactionSource = remember {
                                        MutableInteractionSource()
                                    },
                                    indication = null,
                                    onClick = {
                                        onClick(
                                            this@MainActivity,
                                            this@MainActivity.iPCProvider
                                        )
                                    },
                                )
                        )
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

fun onClick(lifecycleOwner: LifecycleOwner, iPCProvider: IPCProvider) {
    val function = iPCProvider.serviceCreate(ApplicationInfo::class.java)
    lifecycleOwner.lifecycleScope.launch {
        val result = function.packageName
        println("call remote result: $result")
    }
}
