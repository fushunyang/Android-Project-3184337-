package com.example.tree.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.edit
import androidx.navigation.NavController
import com.example.tree.data.UserPreferences
import com.example.tree.data.dataStore
import com.example.tree.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun ResetPasswordScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmNewPassword by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf("") }
    var successMsg by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    Box(modifier = Modifier.fillMaxSize()) {
        MountainBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(screenHeight * 0.3f))

            Text(
                text = "Reset Password",
                style = MaterialTheme.typography.headlineLarge,
                fontSize = 32.sp,
                color = TreeLoginTitleColor,
                modifier = Modifier.padding(bottom = 48.dp)
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TreeLocationIcon,
                    unfocusedBorderColor = TreeProgressTrack
                )
            )
            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = { Text("New Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TreeLocationIcon,
                    unfocusedBorderColor = TreeProgressTrack
                )
            )
            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = confirmNewPassword,
                onValueChange = { confirmNewPassword = it },
                label = { Text("Confirm New Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TreeLocationIcon,
                    unfocusedBorderColor = TreeProgressTrack
                )
            )

            if (errorMsg.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                Text(
                    errorMsg,
                    color = Color.Red,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (successMsg.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                Text(
                    successMsg,
                    color = Color.Green,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = {
                    when {
                        email.isBlank() || newPassword.isBlank() || confirmNewPassword.isBlank() ->
                            errorMsg = "Please fill all fields"
                        newPassword != confirmNewPassword ->
                            errorMsg = "New passwords do not match!"
                        newPassword.length < 6 ->
                            errorMsg = "Password must be at least 6 characters long"
                        else -> {
                            coroutineScope.launch {
                                val trimmedEmail = email.trim()
                                if (!UserPreferences.isEmailRegistered(context, trimmedEmail)) {
                                    errorMsg = "Email not found. Please register first."
                                } else {
                                    // Update password for the email (does not log in automatically)
                                    context.dataStore.edit {
                                        val passwordKey = UserPreferences.passwordKey(trimmedEmail)
                                        it[passwordKey] = newPassword
                                    }
                                    errorMsg = ""
                                    successMsg = "Password reset successfully. Please log in."
                                }
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TreeLoginButtonBg)
            ) {
                Text("Reset Password", fontSize = 18.sp, color = Color.White)
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Back to Login",
                color = TreeLoginSecondaryText,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        navController.navigate("login") {
                            popUpTo("reset_password") { inclusive = true }
                        }
                    }
            )
        }
    }
}