package com.example.taller3compumovil.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.taller3compumovil.R
import com.example.taller3compumovil.viewModel.FirebaseViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random


@Composable
fun MapScreen(
    navToProfile: () -> Unit,
    navLogout: () -> Unit,
    viewModel: FirebaseViewModel,
    modifier: Modifier
) {
    val context = LocalContext.current
    val currentUserState by viewModel.uiState.collectAsState()
    val currentUser = currentUserState.userProfile
    val currentLocation = currentUser.mapPosition.lastOrNull()

    var isLocationOn by remember { mutableStateOf(currentUser.online) }
    val cameraPositionState = rememberCameraPositionState()
    val onlineUsers by viewModel.onlineUsers.collectAsState()

    // Para formatear fecha en snippet
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()) }

    LaunchedEffect(currentLocation) {
        currentLocation?.let {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(
                LatLng(it.latitude, it.longitude),
                14f
            )
        }
    }

    LaunchedEffect(isLocationOn) {
        viewModel.updateOnlineStatus(isLocationOn)
        if (isLocationOn) {
            viewModel.startLocationUpdates()
        } else {
            viewModel.stopLocationUpdates()
        }
    }

    Column(
        modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(onClick = navToProfile) {
                Text(text = stringResource(id = R.string.edit_profile))
            }
            Spacer(Modifier.width(30.dp))
            Button(
                onClick = {
                    isLocationOn = false
                    navLogout()
                }
            ) {
                Text(text = stringResource(id = R.string.logout))
            }
        }
        Spacer(Modifier.height(24.dp))

        GoogleMap(
            modifier.fillMaxSize(0.7f),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                mapStyleOptions = MapStyleOptions.loadRawResourceStyle(
                    context,
                    R.raw.map_style
                )
            )
        ) {
            onlineUsers.forEach { user ->
                val lastPosition = user.mapPosition.lastOrNull()
                lastPosition?.let {
                    Marker(
                        state = MarkerState(LatLng(it.latitude, it.longitude)),
                        title = stringResource(id = R.string.user_title, user.fullName),
                        snippet = stringResource(id = R.string.last_update, dateFormatter.format(Date()))
                    )
                    val path = user.mapPosition.map { pos ->
                        LatLng(pos.latitude, pos.longitude)
                    }
                    if (path.size > 1) {
                        val randomColor = Color(
                            Random.nextFloat(),
                            Random.nextFloat(),
                            Random.nextFloat(),
                            1f
                        )

                        Polyline(
                            points = path,
                            color = randomColor,
                            width = 5f
                        )

                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Filled.LocationOn,
                contentDescription = stringResource(id = R.string.location),
                tint = if (isLocationOn) Color.Green else Color.Red,
                modifier = Modifier.size(48.dp)
            )
            Switch(
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

    DisposableEffect(Unit) {
        onDispose {
            isLocationOn = false
        }
    }
}
