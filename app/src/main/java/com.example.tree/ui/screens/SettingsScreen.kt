package com.example.tree.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.tree.data.UserPreferences
import kotlinx.coroutines.launch
import kotlin.text.isNotEmpty
import kotlin.text.toInt

@Composable
fun SettingsScreen(navController: NavController) {
    val coroutineScope = rememberCoroutineScope()
    var goal by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Settings", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = goal,
            onValueChange = { goal = it },
            label = { Text("Daily Step Goal") }
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = {
            if (goal.isNotEmpty()) {
                coroutineScope.launch {
                    UserPreferences.saveGoal(context, goal.toInt())
                }
            }
        }) {
            Text("Save Goal")
        }
        // Add more settings: notifications, reset tree, color etc.
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { /* Reset tree logic */ }) { Text("Reset Tree") }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { navController.navigate("home") }) { Text("Back") }
    }
}

//TODO：UI