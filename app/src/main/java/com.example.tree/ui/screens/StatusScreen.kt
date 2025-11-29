package com.example.tree.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import com.example.tree.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusScreen(navController: NavController) {
    TreeTheme {
        val context = LocalContext.current
        val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
        val locationManager = remember { context.getSystemService(Context.LOCATION_SERVICE) as LocationManager }
        val colorScheme = MaterialTheme.colorScheme

        // === Sensor Status ===
        val hasStepCounter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) != null
        val hasAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null
        val stepMethod = when {
            hasStepCounter -> "Hardware Step Counter (most accurate & battery-efficient)"
            hasAccelerometer -> "Accelerometer fallback (software detection)"
            else -> "No step detection available"
        }

        // === Location Status ===
        val gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        var currentLocation by remember { mutableStateOf<Location?>(null) }
        var refreshStatus by remember { mutableStateOf("") }

        // ==================== Refresh Function ====================
        val refreshLocation = remember {
            {
                refreshStatus = "Requesting location..."

                val listener = object : LocationListener {
                    override fun onLocationChanged(location: Location) {
                        currentLocation = location
                        refreshStatus = "Location updated successfully! 🌍"
                        locationManager.removeUpdates(this)
                    }

                    override fun onProviderDisabled(provider: String) {
                        refreshStatus = "Location provider disabled"
                    }
                }

                try {
                    if (gpsEnabled) {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0f, listener)
                    } else if (networkEnabled) {
                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0L, 0f, listener)
                    } else {
                        refreshStatus = "Please enable location services in system settings"
                    }
                } catch (e: SecurityException) {
                    refreshStatus = "Permission error: ${e.localizedMessage ?: "Unknown"}"
                }
            }
        }

        // ==================== Permission Launcher ====================
        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                refreshStatus = "Permission granted ✓ Refreshing location..."
                refreshLocation()
            } else {
                refreshStatus = "Location permission denied ✗"
            }
        }

        // ==================== Initial Location ====================
        LaunchedEffect(Unit) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                val lastGps = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                val lastNet = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                currentLocation = lastGps ?: lastNet
            }
        }

        // ==================== UI===================
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        listOf(
                            colorScheme.treeSkyBlue,
                            colorScheme.treeBgTop,
                            colorScheme.treeBgBottom
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Device Status",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.treeTextGood
                )
                Spacer(Modifier.height(48.dp))

                // Pedometer Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceVariant.copy(alpha = 0.95f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
                ) {
                    Column(modifier = Modifier.padding(28.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Sensors, contentDescription = null, tint = colorScheme.treeTextGood)
                            Spacer(Modifier.width(16.dp))
                            Text("Pedometer Sensors", fontSize = 21.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(Modifier.height(20.dp))
                        Text("• Current method: $stepMethod")
                        Text("• Hardware Step Counter: ${if (hasStepCounter) "Available ✓" else "Unavailable ✗"}",
                            color = if (hasStepCounter) colorScheme.treeTextGood else colorScheme.treeTextBad)
                        Text("• Accelerometer: ${if (hasAccelerometer) "Available ✓" else "Unavailable ✗"}",
                            color = if (hasAccelerometer) colorScheme.treeTextGood else colorScheme.treeTextBad)
                    }
                }

                Spacer(Modifier.height(32.dp))

                // Location Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = colorScheme.treeLocationCard.copy(alpha = 0.95f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
                ) {
                    Column(modifier = Modifier.padding(28.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = colorScheme.treeLocationIcon)
                            Spacer(Modifier.width(16.dp))
                            Text("Location Services", fontSize = 21.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(Modifier.height(20.dp))
                        Text("• GPS Provider: ${if (gpsEnabled) "Enabled ✓" else "Disabled ✗"}",
                            color = if (gpsEnabled) colorScheme.treeTextGood else colorScheme.treeTextBad)
                        Text("• Network Provider: ${if (networkEnabled) "Enabled ✓" else "Disabled ✗"}",
                            color = if (networkEnabled) colorScheme.treeTextGood else colorScheme.treeTextBad)

                        Spacer(Modifier.height(24.dp))
                        Text("Current Location:", fontWeight = FontWeight.Medium, fontSize = 17.sp)
                        if (currentLocation != null) {
                            Text("Latitude:  ${String.format(Locale.US, "%.6f", currentLocation!!.latitude)}")
                            Text("Longitude: ${String.format(Locale.US, "%.6f", currentLocation!!.longitude)}")
                            Text("Accuracy:   ${currentLocation!!.accuracy.toInt()} m")
                            Text("Updated:    ${SimpleDateFormat("MMM dd, HH:mm:ss", Locale.getDefault()).format(Date(currentLocation!!.time))}")
                        } else {
                            Text("No location available yet", color = colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                    }
                }

                Spacer(Modifier.height(48.dp))

                // Refresh Button
                val hasPermission = ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

                Button(
                    onClick = {
                        if (hasPermission) refreshLocation() else permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    },
                    modifier = Modifier.fillMaxWidth(0.88f).height(64.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary),
                    shape = MaterialTheme.shapes.large,
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = null, tint = colorScheme.onPrimary)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = if (hasPermission) "Refresh Location" else "Grant Permission & Refresh",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colorScheme.onPrimary
                    )
                }

                if (refreshStatus.isNotEmpty()) {
                    Spacer(Modifier.height(20.dp))
                    Text(
                        text = refreshStatus,
                        color = if (refreshStatus.contains("success", ignoreCase = true) || refreshStatus.contains("✓")) colorScheme.treeTextGood else colorScheme.treeTextBad,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    )
                }

                Spacer(Modifier.height(56.dp))

                OutlinedButton(
                    onClick = { navController.navigate("home") },
                    modifier = Modifier.fillMaxWidth(0.88f),
                    border = ButtonDefaults.outlinedButtonBorder.copy(width = 2.dp)
                ) {
                    Text("Back to Home", fontSize = 17.sp, color = colorScheme.treeTextGood)
                }
            }
        }
    }
}