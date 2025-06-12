package com.jk.chatapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.jk.chatapp.presentation.auth_screen.AuthScreen
import com.jk.chatapp.presentation.home_screen.HomeScreen
import com.jk.chatapp.presentation.navigation.NavGraph
import com.jk.chatapp.presentation.theme.ChatAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChatAppTheme {
                NavGraph()
            }
        }
    }
}