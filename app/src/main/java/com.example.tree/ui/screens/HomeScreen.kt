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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.tree.data.UserPreferences
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlin.math.sqrt
import kotlin.ranges.coerceAtLeast
import kotlin.ranges.coerceAtMost

@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    val accelerometer = remember { sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) }

    var totalSteps by remember { mutableIntStateOf(0) }
    var dailyGoal by remember { mutableIntStateOf(5000) }
    var displaySteps by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        dailyGoal = UserPreferences.goalFlow(context).first()
    }
// push to refresh
    LaunchedEffect(totalSteps) {
        while (isActive) {
            displaySteps = totalSteps
            delay(1000)
        }
    }

    val progress = (totalSteps.toFloat() / dailyGoal.coerceAtLeast(1)).coerceAtMost(1f)
    val treeScale = 0.7f + progress * 1.4f

    val animatedProgress by animateFloatAsState(progress, label = "progress")
    val animatedScale by animateFloatAsState(treeScale, label = "treeScale")

    var previousMagnitude by remember { mutableStateOf<Float?>(null) }
    var lastStepTime by remember { mutableLongStateOf(0L) }

    val stepListener = remember {
        object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event?.sensor?.type != Sensor.TYPE_ACCELEROMETER) return
                val mag = sqrt(event.values[0] * event.values[0] + event.values[1] * event.values[1] + event.values[2] * event.values[2])
                val last = previousMagnitude ?: mag
                previousMagnitude = mag
                val delta = mag - last
                val now = System.currentTimeMillis()

                if (delta > 5f && now - lastStepTime > 100) {
                    totalSteps++
                    lastStepTime = now
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
    }

    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(Unit) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> accelerometer?.let {
                    sensorManager.registerListener(
                        stepListener,
                        it,
                        SensorManager.SENSOR_DELAY_GAME
                    )
                }

                Lifecycle.Event.ON_PAUSE -> sensorManager.unregisterListener(stepListener)
                else -> {}
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            sensorManager.unregisterListener(stepListener)
        }
    }
//Optimize the original box

    Box(modifier = Modifier.fillMaxSize().background(
        brush = Brush.verticalGradient(listOf(Color(0xFFE8F5E9), Color(0xFFB9F6CA), Color.White))
    )) {
        Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(60.dp))

            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.size(210.dp),
                    strokeWidth = 18.dp,
                    color = Color(0xFF4CAF50),
                    trackColor = Color(0x33000000)
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$displaySteps", fontSize = 56.sp, fontWeight = FontWeight.Black, color = Color(0xFF1B5E20))
                    Text("/ $dailyGoal Step", fontSize = 18.sp, color = Color(0xFF2E7D32))
                }
            }

            Spacer(Modifier.height(60.dp))

            Image(
                painter = painterResource(R.drawable.tree),
                contentDescription = "Growing Tree",
                modifier = Modifier.size(280.dp).scale(animatedScale).clip(RoundedCornerShape(32.dp))
            )

            Spacer(Modifier.height(32.dp))

            Card(
                modifier = Modifier.fillMaxWidth(0.9f),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDE7)),
                elevation = CardDefaults.cardElevation(12.dp)
            ) {
                Text(
                    text = if (progress >= 1f) "Goal achieved! Your tree has grown into a towering giant!"
                    else "Every step is watering your sapling.",
                    modifier = Modifier.padding(20.dp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF5D4037),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.weight(1f))

            NavigationBar(containerColor = Color.White.copy(alpha = 0.95f)) {
                NavigationBarItem(selected = false, onClick = { navController.navigate("settings") }, icon = { Icon(Icons.Default.Settings, null) }, label = { Text("Setting") })
                NavigationBarItem(selected = false, onClick = { navController.navigate("history") }, icon = { Icon(Icons.Default.History, null) }, label = { Text("History") })
                NavigationBarItem(selected = true, onClick = { navController.navigate("status") }, icon = { Icon(Icons.Default.Info, null) }, label = { Text("Status") })
            }
        }
    }
}
