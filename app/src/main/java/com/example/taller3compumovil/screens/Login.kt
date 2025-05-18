package com.example.taller3compumovil.screens

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.taller3compumovil.components.CameraComponent
import com.example.taller3compumovil.viewModel.FirebaseViewModel

@Composable
fun loginScreen (onLoginSuccess: () -> Unit,
                 onRegisterClick: () -> Unit,
                 viewModel: FirebaseViewModel,
                 modifier: Modifier) {
    val context = LocalContext.current
    var userEmail by remember { mutableStateOf("") }
    var userPassword by remember { mutableStateOf("") }
    var isEmailError by remember { mutableStateOf(false) }
    var isPasswordError by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    Column (horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth()
            .padding(10.dp)
    ){
        Text(stringResource(R.string.login_title),
            style = TextStyle(
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            ) )
        Spacer(modifier = Modifier.height(10.dp))
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
        Spacer(modifier = Modifier.height(10.dp))
        Text(stringResource(R.string.no_account_text),
            style = TextStyle(
                fontWeight = FontWeight.Bold
            ) )
        TextButton(onClick = onRegisterClick) {
            Text(stringResource(R.string.register_button_text),
                style = TextStyle(
                    fontWeight = FontWeight.Bold
                ) )
        }
        Spacer(modifier = Modifier.height(20.dp))
        Button(onClick = {
            if(userPassword!= null && userEmail!=null){
                viewModel.authenticate(userEmail, userPassword) { error ->
                    if (error == null) {
                        onLoginSuccess()
                    } else {
                        Toast.makeText(context, error.message, Toast.LENGTH_LONG).show()
                    }
                }
            }else{
                Toast.makeText(context, "Ingrese las credenciales", Toast.LENGTH_LONG).show()
            }

        }) {
            Text(stringResource(R.string.login_button_text))
        }

    }
}