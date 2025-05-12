package com.example.taller3compumovil.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.taller3compumovil.R
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun MapScreen(navToProfile: () -> Unit,
              navLogout: () -> Unit){
    var isLocationOn by remember { mutableStateOf(false) }
    val bogota = LatLng(4.7110, -74.0721)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(bogota, 12f)
    }
    val context = LocalContext.current

    Column (modifier = Modifier.fillMaxSize()
        .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally) {

        Spacer(modifier = Modifier.height(30.dp))
        Row(modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
            ){
            Button(onClick = navToProfile) {
                Text(text = "editar perfil")
            }
            Spacer(modifier = Modifier.width(30.dp))
            Button(onClick = navLogout) {
                Text(text = "cerrar sesion")
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        GoogleMap(
            modifier = Modifier.fillMaxHeight(0.7f),
            cameraPositionState = cameraPositionState,
            uiSettings = MapUiSettings(
                zoomControlsEnabled = true,
                compassEnabled = true
            ),
            properties = MapProperties(
                mapStyleOptions = MapStyleOptions.loadRawResourceStyle(
                    context,
                    R.raw.map_style  // Tu archivo JSON en res/raw
                )
            ),
        ) {

        }
        Spacer(modifier = Modifier.height(10.dp))
        Icon(
            imageVector = Icons.Filled.LocationOn,
            contentDescription = "Ubicación",
            tint = Color.Red,
            modifier = Modifier.size(48.dp)

        )
        Switch(
            modifier = Modifier.fillMaxWidth(),
            checked = isLocationOn,
            onCheckedChange = { isLocationOn = it },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.Green,
                checkedTrackColor = Color.Green.copy(alpha = 0.5f),
                uncheckedThumbColor = Color.Red,
                uncheckedTrackColor = Color.Red.copy(alpha = 0.5f)
            )
        )
    }

}