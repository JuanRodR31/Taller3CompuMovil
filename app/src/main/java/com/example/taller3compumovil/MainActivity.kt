package com.example.taller3compumovil

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.taller3compumovil.navigation.NavigationStack
import com.example.taller3compumovil.screens.MapScreen
import com.example.taller3compumovil.ui.theme.Taller3CompuMovilTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Taller3CompuMovilTheme {
                MapScreen(navToProfile = {}, navLogout = {})
            }
        }
    }
}
