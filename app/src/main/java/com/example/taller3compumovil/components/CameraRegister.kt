package com.example.taller3compumovil.components

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil3.compose.AsyncImage
import com.example.taller3compumovil.R
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraComponent(modifier: Modifier = Modifier, onPhotoTaken: (Uri?) -> Unit) {
    var isProcessing by remember { mutableStateOf(false) }
    var profilePic: Uri by rememberSaveable { mutableStateOf(Uri.EMPTY) }

    val context = LocalContext.current
    val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)
    val controller = remember {
        LifecycleCameraController(context).apply {
            setEnabledUseCases(
                CameraController.IMAGE_CAPTURE
            )
        }
    }

    var colors = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
    ) {
        if (cameraPermissionState.status.isGranted) {
            val lifecycleOwner = LocalLifecycleOwner.current
            Column(modifier = Modifier) {
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AndroidView(
                        factory = {
                            PreviewView(it).apply {
                                this.controller = controller
                                controller.bindToLifecycle(lifecycleOwner)
                            }
                        },
                        modifier = modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .border(6.dp, colors.primary, CircleShape)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.Center)
                            .padding(16.dp),
                    ) {
                        IconButton(onClick = {
                            controller.cameraSelector = when (controller.cameraSelector) {
                                CameraSelector.DEFAULT_FRONT_CAMERA -> CameraSelector.DEFAULT_BACK_CAMERA
                                else -> CameraSelector.DEFAULT_FRONT_CAMERA
                            }
                        }, modifier = Modifier) {
                            Icon(
                                Icons.Default.Cameraswitch,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        IconButton(onClick = {
                            isProcessing = true
                            takePhoto(controller, context) { imageUri ->
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.photo_taken_success_label),
                                    Toast.LENGTH_SHORT
                                ).show()
                                imageUri?.let {
                                    profilePic = it
                                    onPhotoTaken(profilePic)
                                }
                                isProcessing = false
                            }
                        }, enabled = !isProcessing) {
                            Icon(
                                Icons.Default.Camera,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = Color.White
                            )
                        }
                    }
                    if (profilePic != Uri.EMPTY) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomStart),
                            horizontalArrangement = Arrangement.End
                        ) {
                            AsyncImage(
                                model = profilePic,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(100.dp)
                                    .aspectRatio(1f)
                                    .clip(CircleShape)
                                    .border(6.dp, MaterialTheme.colorScheme.inverseSurface, CircleShape)
                            )
                        }
                    }
                }
            }
        } else {
            NoCameraPermissionMessage(
                shouldShowRational = cameraPermissionState.status.shouldShowRationale,
                onClick = {
                    cameraPermissionState.launchPermissionRequest()
                })
        }
    }
}

fun takePhoto(
    controller: LifecycleCameraController,
    context: Context,
    onPhotoTaken: (Uri?) -> Unit
) {
    val name = "profile_pic"
    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, name)
        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/")
        }
    }

    val outputOptions = ImageCapture.OutputFileOptions
        .Builder(
            context.contentResolver,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        )
        .build()

    controller.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                Log.i("CUSTOM CAMERA", "Image capture succeeded: ${outputFileResults.savedUri}")
                onPhotoTaken(outputFileResults.savedUri)
            }

            override fun onError(exception: ImageCaptureException) {
                Log.e("CUSTOM CAMERA", "Image capture failed", exception)
            }
        })
}