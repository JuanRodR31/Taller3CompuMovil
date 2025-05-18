package com.example.taller3compumovil.screens

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.taller3compumovil.R
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.taller3compumovil.components.CameraComponent
import com.example.taller3compumovil.data.User
import com.example.taller3compumovil.viewModel.FirebaseViewModel

@Composable
fun registerScreen(onRegisterSuccess: () -> Unit,
                   onAlreadyHasAccount: () -> Unit,
                   viewModel: FirebaseViewModel) {
    var userFullName by remember { mutableStateOf("") }
    var userEmail by remember { mutableStateOf("") }
    var userPassword by remember { mutableStateOf("") }
    var isUserFullNameError by remember { mutableStateOf(false) }
    var isEmailError by remember { mutableStateOf(false) }
    var isPasswordError by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    var profilePic by rememberSaveable { mutableStateOf(Uri.EMPTY) }
    var hasProfilePic by rememberSaveable { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    Column (horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
            .padding(10.dp)
            .verticalScroll(scrollState)
    ){
        Text(stringResource(R.string.register_title),
            style = TextStyle(
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            ) )
        Spacer(modifier = Modifier.height(10.dp))
        Box(modifier = Modifier.size(400.dp)){
            CameraComponent(){
                profilePic  = it
                hasProfilePic = true
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(stringResource(R.string.full_name_title),
            style = TextStyle(
                fontWeight = FontWeight.Bold
            ) )
        OutlinedTextField(
            value = userFullName,
            onValueChange = {
                userFullName = it
                isUserFullNameError = it.isEmpty()
            },
            label = { Text(stringResource(R.string.text_field_full_name_label)) },
            modifier = Modifier.fillMaxWidth(0.8f),
            isError = isUserFullNameError,
            supportingText = {
                if (isUserFullNameError) {
                    Text("Ingresa un nombre completo")
                }
            },
            singleLine = true
        )
        Text(stringResource(R.string.email_title),
            style = TextStyle(
                fontWeight = FontWeight.Bold
            ) )
        OutlinedTextField(
            value = userEmail,
            onValueChange = {
                userEmail = it
                isEmailError = it.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(it).matches()
            },
            label = { Text(stringResource(R.string.text_field_email_label)) },
            modifier = Modifier.fillMaxWidth(0.8f),
            isError = isEmailError,
            supportingText = {
                if (isEmailError) {
                    Text("Ingresa un email válido")
                }
            },
            singleLine = true
        )
        Text(stringResource(R.string.password_title),
            style = TextStyle(
                fontWeight = FontWeight.Bold
            ) )
        OutlinedTextField(
            value = userPassword,
            onValueChange = { userPassword = it },
            label = { Text(stringResource(R.string.text_field_password_label)) },
            modifier = Modifier.fillMaxWidth(0.8f),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = "Toggle password visibility"
                    )
                }
            },
            isError = isPasswordError,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        Text(stringResource(R.string.already_have_account),
            style = TextStyle(
                fontWeight = FontWeight.Light
            ) )
        Spacer(modifier = Modifier.height(10.dp))
        TextButton(onClick = onAlreadyHasAccount) {
            Text(stringResource(R.string.login_title),
                style = TextStyle(
                    fontWeight = FontWeight.Light
                ) )
        }

        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = {
                if (userFullName.isNotEmpty() && userEmail.isNotEmpty() && userPassword.isNotEmpty() && !isEmailError && hasProfilePic) {
                    isLoading = true
                    viewModel.register(userEmail, userPassword, userFullName, profilePic) { error ->
                        isLoading = false
                        if (error == null) {
                            // Registro exitoso, también autentica al usuario
                            viewModel.authenticate(userEmail, userPassword) { authError ->
                                if (authError == null) {

                                }
                            }
                        } else {
                            Toast.makeText(
                                context,
                                "Error: ${error.localizedMessage}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                } else {
                    isEmailError = userEmail.isEmpty()
                }
            },
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(50.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White)
            } else {
                Text(
                    text = stringResource(R.string.register_button_text),
                )
            }
        }
    }
}