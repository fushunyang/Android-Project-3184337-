package com.example.tree.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.tree.R
import com.example.tree.data.UserPreferences
import com.example.tree.ui.theme.TreeBgBottom
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val isLoggedIn by UserPreferences.isLoggedInFlow(context).collectAsState(initial = false)

    LaunchedEffect(isLoggedIn) {
        delay(1400) //
        if (isLoggedIn) {
            navController.navigate("home") {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        } else {
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TreeBgBottom),  // Keep as fallback if image loads slowly
        contentAlignment = Alignment.Center
    ) {
        // Full-screen background image: Fill the entire Box
        Image(
            painter = painterResource(R.drawable.background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),  // Full-screen fill
            contentScale = ContentScale.Crop
        )

        // Foreground elements: Overlay on the image (progress bar + text)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.align(Alignment.Center)  // Keep centered
        ) {


            CircularProgressIndicator(
                color = Color(0xFF4CAF50) ,
                strokeWidth = 7.dp,
                modifier = Modifier.size(48.dp)
            )
            Spacer(Modifier.height(32.dp))
            Text(
                "waking up your little tree...",
                fontSize = 22.sp,
                color = Color.White.copy(alpha = 0.95f)
            )
        }
    }
}