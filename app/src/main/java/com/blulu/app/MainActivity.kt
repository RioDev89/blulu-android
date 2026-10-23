package com.blulu.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.blulu.app.ui.ChatScreen
import com.blulu.app.ui.ChatViewModel
import com.blulu.app.ui.SettingsScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                Surface(Modifier.fillMaxSize()) {
                    BluluApp()
                }
            }
        }
    }
}

@Composable
fun BluluApp(vm: ChatViewModel = viewModel()) {
    var onSettings by rememberSaveable { mutableStateOf(false) }
    if (onSettings) {
        SettingsScreen(vm) { onSettings = false }
    } else {
        ChatScreen(vm) { onSettings = true }
    }
}
