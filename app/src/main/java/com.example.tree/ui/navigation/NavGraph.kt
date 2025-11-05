// ui/navigation/NavGraph.kt
package com.example.tree.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.tree.ui.screens.HistoryScreen
import com.example.tree.ui.screens.HomeScreen
import com.example.tree.ui.screens.LoginScreen
import com.example.tree.ui.screens.SettingsScreen
import com.example.tree.ui.screens.StatusScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "login") {
        composable("login") { LoginScreen(navController) }
        composable("home") { HomeScreen(navController) }
        composable("settings") { SettingsScreen(navController) }
        composable("history") { HistoryScreen(navController) }
        composable("status") { StatusScreen(navController) }
        //composable(route="Password"){PasswordMnanger(navController)}
        //0.0 TRYING TO MAKE IT LATER,LOL
    }
}