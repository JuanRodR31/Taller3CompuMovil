package com.example.taller3compumovil.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.example.taller3compumovil.viewModel.FirebaseViewModel
import com.example.taller3compumovil.R

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    viewModel: FirebaseViewModel,
    navController: NavController
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var isEditingProfile by remember { mutableStateOf(false) }
    var isNameError by remember { mutableStateOf(false) }
    var isPhoneError by remember { mutableStateOf(false) }

    var isEditingPassword by remember { mutableStateOf(false) }
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isCurrentPasswordError by remember { mutableStateOf(false) }
    var isNewPasswordError by remember { mutableStateOf(false) }
    var isConfirmPasswordError by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.getUser { } }

    LaunchedEffect(uiState.userProfile) {
        name = uiState.userProfile.fullName
        phone = uiState.userProfile.phone.takeIf { it != 0L }?.toString() ?: ""
    }

    Box(modifier = modifier.fillMaxSize().padding(27.dp)
    ) {
        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier.align(Alignment.TopStart).padding(8.dp)
        ) {
            Icon(
                Icons.Default.ArrowBack,
                contentDescription = stringResource(R.string.back),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        Column(
            Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AsyncImage(
                model = uiState.userProfile.photoUrl.takeIf { it.isNotEmpty() },
                contentDescription = stringResource(R.string.profile_photo),
                modifier = Modifier.size(120.dp).clip(CircleShape)
            )

            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    isNameError = it.isBlank() || it.length > 50
                },
                label = { Text(stringResource(R.string.full_name)) },
                singleLine = true,
                isError = isNameError,
                enabled = isEditingProfile && !isLoading,
                leadingIcon = { Icon(Icons.Default.Person, null) },
                modifier = Modifier.fillMaxWidth()
            )
            if (isNameError) Text(stringResource(R.string.name_error), color = MaterialTheme.colorScheme.error)

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = phone,
                onValueChange = {
                    phone = it.filter { c -> c.isDigit() }
                    isPhoneError = phone.isNotEmpty() && (phone.length < 7 || phone.length > 15)
                },
                label = { Text(stringResource(R.string.phone)) },
                singleLine = true,
                isError = isPhoneError,
                enabled = isEditingProfile && !isLoading,
                leadingIcon = { Icon(Icons.Default.Phone, null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth()
            )
            if (isPhoneError) Text(stringResource(R.string.phone_error), color = MaterialTheme.colorScheme.error)

            Spacer(Modifier.height(16.dp))

            if (isEditingProfile) {
                Button(
                    onClick = {
                        isLoading = true
                        viewModel.updateUser(uiState.userProfile.copy(
                            fullName = name,
                            phone = phone.toLongOrNull() ?: 0L
                        )) { error ->
                            isLoading = false
                            if (error == null) {
                                Toast.makeText(context, context.getString(R.string.profile_updated), Toast.LENGTH_SHORT).show()
                                isEditingProfile = false
                            } else {
                                Toast.makeText(context, "${context.getString(R.string.error)}: ${error.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    enabled = !isNameError && !isPhoneError,
                    modifier = Modifier.fillMaxWidth()
                ) { Text(stringResource(R.string.save)) }

                Button(
                    onClick = {
                        isEditingProfile = false
                        name = uiState.userProfile.fullName
                        phone = uiState.userProfile.phone.takeIf { it != 0L }?.toString() ?: ""
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text(stringResource(R.string.cancel)) }
            } else {
                Button(
                    onClick = { isEditingProfile = true },
                    modifier = Modifier.fillMaxWidth()
                ) { Text(stringResource(R.string.edit_profile)) }
            }

            Spacer(Modifier.height(32.dp))

            if (isEditingPassword) {
                OutlinedTextField(
                    value = currentPassword,
                    onValueChange = {
                        currentPassword = it
                        isCurrentPasswordError = it.isBlank()
                    },
                    label = { Text(stringResource(R.string.current_password)) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth()
                )
                if (isCurrentPasswordError) Text(stringResource(R.string.required_field), color = MaterialTheme.colorScheme.error)

                OutlinedTextField(
                    value = newPassword,
                    onValueChange = {
                        newPassword = it
                        isNewPasswordError = it.isNotBlank() && it.length < 6
                    },
                    label = { Text(stringResource(R.string.new_password)) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth()
                )
                if (isNewPasswordError) Text(stringResource(R.string.min_6_chars), color = MaterialTheme.colorScheme.error)

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                        isConfirmPasswordError = it != newPassword
                    },
                    label = { Text(stringResource(R.string.confirm_password)) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth()
                )
                if (isConfirmPasswordError) Text(stringResource(R.string.passwords_do_not_match), color = MaterialTheme.colorScheme.error)

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (!isCurrentPasswordError && !isNewPasswordError && !isConfirmPasswordError) {
                            isLoading = true
                            viewModel.updatePassword(currentPassword, newPassword) { error ->
                                isLoading = false
                                if (error == null) {
                                    Toast.makeText(context, context.getString(R.string.password_updated), Toast.LENGTH_SHORT).show()
                                    isEditingPassword = false
                                    currentPassword = ""
                                    newPassword = ""
                                    confirmPassword = ""
                                } else {
                                    Toast.makeText(context, "${context.getString(R.string.error)}: ${error.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    },
                    enabled = !isCurrentPasswordError && !isNewPasswordError && !isConfirmPasswordError,
                    modifier = Modifier.fillMaxWidth()
                ) { Text(stringResource(R.string.save_password)) }

                Button(
                    onClick = {
                        isEditingPassword = false
                        currentPassword = ""
                        newPassword = ""
                        confirmPassword = ""
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text(stringResource(R.string.cancel)) }
            } else {
                Button(
                    onClick = { isEditingPassword = true },
                    modifier = Modifier.fillMaxWidth()
                ) { Text(stringResource(R.string.change_password)) }
            }
        }
    }
}
