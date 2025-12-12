package com.example.tree.ui.screens

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.tree.R
import com.example.tree.data.UserPreferences
import com.example.tree.data.dataStore
import com.example.tree.ui.theme.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.font.FontWeight
import androidx.datastore.preferences.core.edit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    TreeTheme {
        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()
        val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }

        val currentEmail by UserPreferences.savedEmailFlow(context).collectAsState(initial = "")

        val dailyGoal by UserPreferences.goalFlow(context).collectAsState(initial = 10000)
        val dailySteps by UserPreferences.dailyStepsFlow(context).collectAsState(initial = 0)

        val progress = (dailySteps.toFloat() / dailyGoal.coerceAtLeast(1)).coerceAtMost(1f)
        val animatedProgress by animateFloatAsState(progress, tween(1200))

        val treeStage = ((dailySteps / 10) + 1).coerceIn(1, 7)

        /* ====================== Auto-check date on app launch and save history if needed ====================== */
        LaunchedEffect(Unit) {
            if (currentEmail.isBlank()) return@LaunchedEffect

            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val data = context.dataStore.data.first()

            val lastDateKey = UserPreferences.lastDateKey(currentEmail)
            val dailyKey = UserPreferences.dailyStepsKey(currentEmail)

            val savedDate = data[lastDateKey] ?: today
            val currentDaily = data[dailyKey] ?: 0

            if (savedDate != today && currentDaily > 0) {
                UserPreferences.savePreviousDayToHistory(context, savedDate, currentDaily)

                context.dataStore.edit {
                    it[dailyKey] = 0
                    it[lastDateKey] = today
                }
            }
        }

        /* ====================== Ultimate Step Counting System: Hardware Counter > Detector > Accelerometer Fallback ====================== */
        DisposableEffect(currentEmail) {
            var registeredListener: SensorEventListener? = null

            if (currentEmail.isNotBlank()) {
                val stepCounter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
                val stepDetector = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
                val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

                // Unified +1 step logic (shared by detector & accelerometer)
                val incrementStep = {
                    coroutineScope.launch {
                        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                        val data = context.dataStore.data.first()

                        val lastDateKey = UserPreferences.lastDateKey(currentEmail)
                        val dailyKey = UserPreferences.dailyStepsKey(currentEmail)

                        val savedDate = data[lastDateKey] ?: today
                        var currentDaily = data[dailyKey] ?: 0

                        if (savedDate != today) {
                            if (currentDaily > 0) {
                                UserPreferences.savePreviousDayToHistory(context, savedDate, currentDaily)
                            }
                            currentDaily = 0
                        }

                        currentDaily += 1

                        context.dataStore.edit {
                            it[dailyKey] = currentDaily
                            it[lastDateKey] = today
                        }
                    }
                }

                val listener = if (stepCounter != null) {
                    // Hardware Step Counter (most accurate method)
                    object : SensorEventListener {
                        override fun onSensorChanged(event: SensorEvent?) {
                            event ?: return
                            val total = event.values[0].toLong()

                            coroutineScope.launch {
                                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                                val data = context.dataStore.data.first()

                                val lastDateKey = UserPreferences.lastDateKey(currentEmail)
                                val lastTotalKey = UserPreferences.lastTotalStepsKey(currentEmail)
                                val dailyKey = UserPreferences.dailyStepsKey(currentEmail)

                                val savedDate = data[lastDateKey] ?: today
                                val savedTotal = data[lastTotalKey] ?: total
                                var currentDaily = data[dailyKey] ?: 0

                                var delta = total - savedTotal
                                if (delta < 0) delta = total

                                if (savedDate != today) {
                                    if (currentDaily > 0) {
                                        UserPreferences.savePreviousDayToHistory(context, savedDate, currentDaily)
                                    }
                                    currentDaily = delta.toInt().coerceAtLeast(0)
                                } else {
                                    currentDaily += delta.toInt().coerceAtLeast(0)
                                }

                                context.dataStore.edit {
                                    it[dailyKey] = currentDaily
                                    it[lastTotalKey] = total
                                    it[lastDateKey] = today
                                }
                            }
                        }

                        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
                    }
                } else if (stepDetector != null) {
                    // Step Detector (increments +1 per event) - FIXED: Ignore initial spurious event
                    object : SensorEventListener {
                        private var ignoreInitialEvent = true

                        override fun onSensorChanged(event: SensorEvent?) {
                            if (ignoreInitialEvent) {
                                ignoreInitialEvent = false
                                return
                            }
                            if (event?.values?.get(0) == 1.0f) incrementStep()
                        }

                        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
                    }
                } else {
                    // Accelerometer (high-sensitivity mode: simple peak detection) - OPTIMIZED: Less sensitive
                    var lastStepTime = 0L
                    var lastMagnitude = 0f
                    val stepThreshold = 1.5f  // OPTIMIZED: Raised from 1.2f to reduce false positives
                    val minStepInterval = 500L  // OPTIMIZED: Increased from 400L for better noise filtering

                    object : SensorEventListener {
                        private var ignoreInitialEvent = true

                        override fun onSensorChanged(event: SensorEvent?) {
                            if (ignoreInitialEvent) {
                                ignoreInitialEvent = false
                                event?.let { lastMagnitude = sqrt(it.values[0]*it.values[0] + it.values[1]*it.values[1] + it.values[2]*it.values[2]) }
                                return
                            }
                            event ?: return
                            val x = event.values[0]
                            val y = event.values[1]
                            val z = event.values[2]
                            val magnitude = sqrt(x * x + y * y + z * z)
                            val currentTime = System.currentTimeMillis()

                            // Detect peak: Magnitude exceeds threshold, increasing from previous, and sufficient time elapsed
                            if (magnitude > stepThreshold &&
                                magnitude > lastMagnitude &&
                                (currentTime - lastStepTime) > minStepInterval) {
                                incrementStep()
                                lastStepTime = currentTime
                            }

                            lastMagnitude = magnitude
                        }

                        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
                    }
                }

                val sensorToUse = stepCounter ?: stepDetector ?: accelerometer!!
                val delay = if (sensorToUse.type == Sensor.TYPE_ACCELEROMETER) SensorManager.SENSOR_DELAY_GAME else SensorManager.SENSOR_DELAY_UI
                sensorManager.registerListener(listener, sensorToUse, delay)
                registeredListener = listener
            }

            onDispose {
                registeredListener?.let { sensorManager.unregisterListener(it) }
            }
        }

        /* ================================== UI (Using Crossfade for smooth scene transitions) ================================== */
        Box(modifier = Modifier.fillMaxSize()) {
            // Use Crossfade to smoothly transition background images
            Crossfade(
                targetState = treeStage,
                modifier = Modifier.fillMaxSize(),
                animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
                label = "stage_transition"
            ) { currentStage ->
                val currentResourceId = when (currentStage) {
                    1 -> R.drawable.stage1
                    2 -> R.drawable.stage2
                    3 -> R.drawable.stage3
                    4 -> R.drawable.stage4
                    5 -> R.drawable.stage5
                    6 -> R.drawable.stage6
                    else -> R.drawable.stage7
                }
                Image(
                    painter = painterResource(currentResourceId),
                    contentDescription = "Tree Stage with Background",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            // Foreground UI: Progress ring on top, buttons at bottom
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top section: Progress ring
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(modifier = Modifier.height(40.dp))

                    CreativeProgressRing(
                        progress = animatedProgress,
                        dailySteps = dailySteps,
                        dailyGoal = dailyGoal
                    )
                }

                // Bottom section: Button row (with gradient overlay and text labels)
                Spacer(modifier = Modifier.height(40.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                listOf(
                                    OverlayTransparent,
                                    OverlayWhiteSemi,
                                    OverlayWhiteOpaque
                                )
                            )
                        )
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // History button: Icon + text
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable { navController.navigate("history") }
                                .padding(8.dp)
                        ) {
                            Icon(
                                Icons.Default.History,
                                contentDescription = "History Screen",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                            Text(
                                text = "History Screen",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Status button: Icon + text
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable { navController.navigate("status") }
                                .padding(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = "Status Screen",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                            Text(
                                text = "Status Screen",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Settings button: Icon + text
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable { navController.navigate("settings") }
                                .padding(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Settings,
                                contentDescription = "Setting Screen",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                            Text(
                                text = "Setting Screen",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

/* ====================== Progress Ring (with FastOutSlowInEasing animation) ====================== */
@Composable
private fun CreativeProgressRing(progress: Float, dailySteps: Int, dailyGoal: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "ring")

    val rotatingAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 16000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "leaf_rotation"
    )

    val pulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(modifier = Modifier.size(320.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasSize = minOf(size.width, size.height)
            val ringRadius = canvasSize / 2f - 40.dp.toPx()
            val strokeWidth = 20.dp.toPx()
            val center = Offset(size.width / 2f, size.height / 2f)

            // Background ring (light blue full ring)
            drawCircle(
                brush = Brush.sweepGradient(
                    listOf(
                        ProgressBgStart.copy(0.6f),
                        ProgressBgMid1.copy(0.7f),
                        ProgressBgMid2.copy(0.9f),
                        ProgressBgEnd.copy(0.9f)
                    )
                ),
                radius = ringRadius,
                center = center,
                style = Stroke(strokeWidth, cap = StrokeCap.Round)
            )

            // Progress green ring (pixel-perfect alignment, no vertical bars, no offsets)
            if (progress > 0f) {
                val arcDiameter = ringRadius * 2f
                val arcSize = Size(arcDiameter, arcDiameter)
                val arcTopLeft = Offset(center.x - ringRadius, center.y - ringRadius)

                drawArc(
                    brush = Brush.sweepGradient(
                        listOf(
                            ProgressGreenDark,
                            ProgressGreen,
                            ProgressGreenLight1,
                            ProgressGreenLight2
                        )
                    ),
                    startAngle = -90f,
                    sweepAngle = 360f * progress,
                    useCenter = false,
                    topLeft = arcTopLeft,
                    size = arcSize,
                    style = Stroke(strokeWidth, cap = StrokeCap.Round)
                )
            }

            // Floating leaves (perfectly aligned on the same center and radius)
            val leafCount = (progress * 15).toInt().coerceIn(3, 15)
            for (i in 0 until leafCount) {
                val angle = (360f / leafCount) * i + rotatingAngle
                val rad = Math.toRadians(angle.toDouble())
                val leafRadius = ringRadius * 0.72f
                val x = center.x + leafRadius * cos(rad).toFloat()
                val y = center.y + leafRadius * sin(rad).toFloat()
                drawCircle(
                    color = LeafGreen.copy(alpha = 0.75f + progress * 0.25f),
                    radius = 12f,
                    center = Offset(x, y)
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.scale(pulse)) {
            Text("$dailySteps", fontSize = 78.sp, fontWeight = FontWeight.Black, color = TreeStepBig)
            Text("/ $dailyGoal steps", fontSize = 19.sp, color = TreeGoalText)
        }

        if (progress > 0.01f) {
            Text(
                "Every step makes your little tree more lush!",
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 20.dp)
            )
        }
    }
}