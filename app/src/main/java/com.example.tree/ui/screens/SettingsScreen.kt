package com.example.tree.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk  // Import for the icon
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.navigation.NavController
import com.example.tree.data.UserPreferences
import com.example.tree.data.dataStore
import com.example.tree.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Collect current email for key generation
    val currentEmail by UserPreferences.savedEmailFlow(context).collectAsState(initial = "")

    // Real-time read current goal steps
    val currentGoal by UserPreferences.goalFlow(context).collectAsState(initial = 10000)
    var goalText by remember(currentGoal) { mutableStateOf(currentGoal.toString()) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFFDF7)) // Exact light beige background matching screenshot
    ) {
        Card(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(0.88f),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Daily\nStep Goal",
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF212121),
                    textAlign = TextAlign.Center,
                    lineHeight = 48.sp
                )

                Spacer(modifier = Modifier.height(60.dp))

                // Input field (exact style from screenshot)
                OutlinedTextField(
                    value = goalText,
                    onValueChange = { input ->
                        // Only allow numeric input
                        if (input.isEmpty() || input.all { it.isDigit() }) {
                            goalText = input
                        }
                    },
                    leadingIcon = {
                        Icon(
                            Icons.AutoMirrored.Filled.DirectionsWalk,
                            contentDescription = null,
                            tint = ProgressGreen
                        )
                    },
                    placeholder = {
                        Text(
                            "Daily Step Goal",
                            color = Color(0xFF999999),
                            fontSize = 17.sp
                        )
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(32.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = ProgressGreen,
                        unfocusedBorderColor = TreeGreen,
                        cursorColor = ProgressGreen
                    )
                )

                Spacer(modifier = Modifier.height(48.dp))

                // Save Goal green button (with ✓ icon)
                Button(
                    onClick = {
                        val newGoal = goalText.toIntOrNull() ?: 10000
                        if (newGoal > 0) {
                            coroutineScope.launch {
                                UserPreferences.saveGoal(context, newGoal.coerceAtLeast(1000))
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    shape = RoundedCornerShape(32.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ProgressGreen),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Filled.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Save Goal",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Reset Tree button
                Button(
                    onClick = {
                        if (currentEmail.isNotBlank()) {  // Guard against blank email
                            coroutineScope.launch {
                                context.dataStore.edit { prefs ->
                                    // 1. Immediately reset daily steps to 0 → Tree reverts to seedling
                                    val dailyStepsKey = intPreferencesKey("daily_steps_$currentEmail")
                                    prefs[dailyStepsKey] = 0
                                    // 2. Remove cumulative base and date records
                                    //    Next walk will use current sensor value as new starting point, perfect reset from 0
                                    val lastTotalStepsKey = longPreferencesKey("last_total_steps_$currentEmail")
                                    val lastDateKey = stringPreferencesKey("last_date_$currentEmail")
                                    prefs.remove(lastTotalStepsKey)
                                    prefs.remove(lastDateKey)
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    shape = RoundedCornerShape(32.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEEEEEE)),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Text(
                        "Reset Tree",
                        fontSize = 18.sp,
                        color = Color(0xFF666666),  // Matches TreeLoginSecondaryText
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Logout button
                Button(
                    onClick = {
                        coroutineScope.launch {
                            val isLoggedInKey = booleanPreferencesKey("is_logged_in")
                            context.dataStore.edit { prefs ->
                                prefs[isLoggedInKey] = false
                            }
                            // Navigate back to login screen and clear the back stack
                            navController.navigate("login") {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    shape = RoundedCornerShape(32.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEEEEEE)),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Text(
                        "log out ",
                        fontSize = 18.sp,
                        color = Color(0xFF666666),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Back to Home
        Text(
            text = "Back to Home",
            fontSize = 17.sp,
            color = Color(0xFF666666),  // Matches TreeLoginSecondaryText
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 60.dp)
                .clickable {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = false }
                    }
                }
        )
    }
}