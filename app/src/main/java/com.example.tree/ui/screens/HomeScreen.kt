package com.example.tree.ui.screens

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.tree.R
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.Color
import androidx.datastore.preferences.core.edit
import com.example.tree.data.UserPreferences
import com.example.tree.data.dataStore
import com.example.tree.ui.theme.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.min
import kotlin.math.sqrt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    TreeTheme {
        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()
        val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
        val colorScheme = MaterialTheme.colorScheme

        val dailyGoal by UserPreferences.goalFlow(context).collectAsState(initial = 10000)
        val dailySteps by UserPreferences.dailyStepsFlow(context).collectAsState(initial = 0)
        val progress = (dailySteps.toFloat() / dailyGoal.coerceAtLeast(1)).coerceAtMost(1f)
        val treeScale = 0.7f + progress * 1.4f
        val animatedProgress by animateFloatAsState(progress)
        val animatedScale by animateFloatAsState(treeScale)

        // Calculate tree stage based on steps (every 1000 steps, up to 7 stages)
        val treeStage = min(7, (dailySteps / 1000) + 1)
        val treeResourceId = when (treeStage) {
            1 -> R.drawable.stage1 //  (stage 1: 0-999 steps)
            2 -> R.drawable.stage2 //  (stage 2: 1000-1999 steps)
            3 -> R.drawable.stage3 //  (stage 3: 2000-2999 steps)
            4 -> R.drawable.stage4 //  (stage 4: 3000-3999 steps)
            5 -> R.drawable.stage5 //  (stage 5: 4000-4999 steps)
            6 -> R.drawable.stage6 //  (stage 6: 5000-5999 steps)
            7 -> R.drawable.stage7 //  (stage 7: 6000+ steps)
            else -> R.drawable.stage1
        }

        // step count
        val stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        if (stepCounterSensor != null) {
            val listener = remember {
                object : SensorEventListener {
                    override fun onSensorChanged(event: SensorEvent?) {
                        event ?: return
                        val total = event.values[0].toLong()
                        coroutineScope.launch {
                            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                            val data = context.dataStore.data.first()
                            val savedDate = data[UserPreferences.LAST_DATE_KEY] ?: today
                            val savedTotal = data[UserPreferences.LAST_TOTAL_STEPS_KEY] ?: total
                            var currentDaily = data[UserPreferences.DAILY_STEPS_KEY] ?: 0

                            var delta = total - savedTotal
                            if (delta < 0) delta = total

                            if (savedDate != today) currentDaily = delta.toInt().coerceAtLeast(0)
                            else currentDaily += delta.toInt().coerceAtLeast(0)

                            context.dataStore.edit {
                                it[UserPreferences.DAILY_STEPS_KEY] = currentDaily
                                it[UserPreferences.LAST_TOTAL_STEPS_KEY] = total
                                it[UserPreferences.LAST_DATE_KEY] = today
                            }
                        }
                    }
                    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
                }
            }
            DisposableEffect(Unit) {
                sensorManager.registerListener(listener, stepCounterSensor, SensorManager.SENSOR_DELAY_NORMAL)
                onDispose { sensorManager.unregisterListener(listener) }
            }
        } else {
            // Accelerometer
            val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            if (accelerometer != null) {
                var lastStepTime by remember { mutableLongStateOf(0L) }
                val listener = remember {
                    object : SensorEventListener {
                        override fun onSensorChanged(event: SensorEvent?) {
                            event ?: return
                            val mag = sqrt(event.values[0]*event.values[0] + event.values[1]*event.values[1] + event.values[2]*event.values[2])
                            if (mag > 13.5f) {
                                val now = System.currentTimeMillis()
                                if (now - lastStepTime > 380) {
                                    coroutineScope.launch {
                                        context.dataStore.edit {
                                            it[UserPreferences.DAILY_STEPS_KEY] = (dailySteps + 1).coerceAtLeast(0)
                                        }
                                    }
                                    lastStepTime = now
                                }
                            }
                        }
                        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
                    }
                }
                DisposableEffect(Unit) {
                    sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_UI)
                    onDispose { sensorManager.unregisterListener(listener) }
                }
            }
        }

        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                NavigationBar(containerColor = colorScheme.surface.copy(alpha = 0.96f), tonalElevation = 20.dp) {
                    NavigationBarItem(
                        selected = false,
                        onClick = { navController.navigate("settings") },
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                        label = { Text("Settings", color = TreeTextGood.copy(alpha = 0.8f)) }
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = { navController.navigate("history") },
                        icon = { Icon(Icons.Default.History, contentDescription = "History") },
                        label = { Text("History", color = TreeTextGood.copy(alpha = 0.8f)) }
                    )
                    NavigationBarItem(
                        selected = true,
                        onClick = { navController.navigate("status") },
                        icon = { Icon(Icons.Default.Info, contentDescription = "Status") },
                        label = { Text("Status", color = colorScheme.primary) }
                    )
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            listOf(colorScheme.treeBgTop, colorScheme.treeBgMid1, colorScheme.treeBgMid2, colorScheme.treeBgBottom)
                        )
                    )
                    .padding(paddingValues)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 28.dp, vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier.height(60.dp))

                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier.size(240.dp),
                            strokeWidth = 22.dp,
                            color = colorScheme.primary,
                            trackColor = colorScheme.outline.copy(alpha = 0.3f)
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("$dailySteps", fontSize = 64.sp, fontWeight = FontWeight.Black, color = TreeStepBig)
                            Text("/ $dailyGoal steps", fontSize = 20.sp, color = TreeGoalText)
                        }
                    }

                    Spacer(Modifier.height(80.dp))

                    Image(
                        painter = painterResource(treeResourceId),
                        contentDescription = "Your Growing Tree - Stage $treeStage",
                        modifier = Modifier.size(320.dp).scale(animatedScale).clip(RoundedCornerShape(40.dp))
                    )

                    Spacer(Modifier.height(40.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(0.92f),
                        shape = RoundedCornerShape(36.dp),
                        colors = CardDefaults.cardColors(containerColor = colorScheme.surface.copy(alpha = 0.97f)),
                        elevation = CardDefaults.cardElevation(16.dp)
                    ) {
                        Text(
                            text = when {
                                progress >= 1f -> "Goal achieved! Your tree is now a towering giant! 🌳✨"
                                progress >= 0.8f -> "Almost there! Just a few more steps!"
                                progress >= 0.5f -> "Halfway done! Your tree is thriving!"
                                else -> "Every step waters your little sapling. Keep going! 🌱"
                            },
                            modifier = Modifier.padding(24.dp),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}