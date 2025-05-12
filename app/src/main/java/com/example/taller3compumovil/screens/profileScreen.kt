package com.example.taller3compumovil.screens

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.outlined.Construction
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.taller3compumovil.R
import com.example.taller3compumovil.data.User
import com.example.taller3compumovil.viewModel.FirebaseViewModel
import com.google.firebase.Firebase
import com.google.firebase.storage.storage

@Composable
fun ProfileScreen(modifier: Modifier = Modifier, viewModel: FirebaseViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val storageRef = Firebase.storage.getReference("users/${uiState.user?.uid}/profile.jpg")

    var profileImage by remember { mutableStateOf(Uri.EMPTY) }
    var name by remember { mutableStateOf("") }
    var isNameError by remember { mutableStateOf(false) }
    var isPhoneError by remember { mutableStateOf(false) }

    var loading by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.getUser(){ currentUser ->
            name = currentUser?.fullName ?: ""
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(modifier = Modifier
            .size(300.dp)
            .aspectRatio(1f)
            .clip(CircleShape)) {
            storageRef.downloadUrl.addOnSuccessListener { url ->
                profileImage = url
            }
            AsyncImage(
                model = profileImage,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                placeholder = rememberVectorPainter(Icons.Outlined.Construction)
            )
        }
        OutlinedTextField(
            onValueChange = {
                isNameError = it.isEmpty() || it.length > 50
                name = it
            },
            singleLine = true,
            value = name,
            isError = isNameError,
            label = { Text(stringResource(R.string.full_name_title)) },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
            trailingIcon = {
                if (isNameError)
                    Icon(
                        Icons.Filled.Error,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
            },
            placeholder = { Text(stringResource(R.string.text_field_full_name_label)) },
            supportingText = {
                if (isNameError) {
                    Text(stringResource(R.string.name_error))
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            enabled = !loading,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
        )
        Button(
            enabled = !isNameError && !isPhoneError,
            onClick = {
                if (!isNameError && !isPhoneError) {
                    loading = true
                    viewModel.updateUser(newInfo = User(name)) { error ->
                        if (error != null) {
                            Toast.makeText(
                                context,
                                error.localizedMessage,
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            Toast.makeText(
                                context,
                                context.getString(R.string.profile_updated_successfully),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        loading = false
                    }
                }
            }, modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.update_profile))
        }
    }
}