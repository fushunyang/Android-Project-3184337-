
package com.example.tree.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
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
fun LoginScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf("") }
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
                text = "Login",
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TreeLocationIcon,
                        unfocusedBorderColor = TreeProgressTrack
                    )
                )
                Text(
                    text = "Forgot?",
                    color = TreeLoginSecondaryText,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .clickable {
                            navController.navigate("reset_password")
                        }
                )
            }

            if (errorMsg.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                Text(errorMsg, color = Color.Red, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            }

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = {
                    when {
                        email.isBlank() || password.isBlank() -> errorMsg = "Please fill all fields"
                        else -> {
                            coroutineScope.launch {
                                if (UserPreferences.verifyCredentials(context, email, password)) {
                                    // Update current user (switches account if different)
                                    context.dataStore.edit { it[UserPreferences.EMAIL_KEY] = email.trim() }
                                    // Ensure logged in state
                                    context.dataStore.edit { it[UserPreferences.IS_LOGGED_IN_KEY] = true }
                                    navController.navigate("home") {
                                        popUpTo(0) { inclusive = true }
                                        launchSingleTop = true
                                    }
                                    errorMsg = ""  // Clear error
                                } else {
                                    errorMsg = "Invalid email or password"
                                }
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TreeLoginButtonBg,
                    contentColor = Color.White
                )
            ) {
                Text("Log In", fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Don't have account? Create now",
                color = TreeLoginSecondaryText,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        navController.navigate("register") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
            )
        }
    }
}

@Composable
fun MountainBackground() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawRect(color = TreeLocationCard)
        val path = Path().apply {
            moveTo(0f, size.height * 0.28f)
            quadraticBezierTo(size.width / 2, size.height * 0.50f, size.width, size.height * 0.28f)
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }
        drawPath(path, Color.White)
    }
}