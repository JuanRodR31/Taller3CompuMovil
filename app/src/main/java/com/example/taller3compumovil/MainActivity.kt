package com.example.taller3compumovil

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.taller3compumovil.screens.loginScreen
import com.example.taller3compumovil.screens.registerScreen
import com.example.taller3compumovil.ui.theme.Taller3CompuMovilTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Taller3CompuMovilTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    registerScreen(onLoginSuccess = {},
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
