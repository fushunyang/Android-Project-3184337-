package com.example.tree.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.navigation.NavController
import com.example.tree.data.UserPreferences
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(navController: NavController) {
    val context = LocalContext.current

    val savedHistory by UserPreferences.historyStepsFlow(context).collectAsState(initial = emptyList())
    val todaySteps by UserPreferences.dailyStepsFlow(context).collectAsState(initial = 0)
    val todayDateStr = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
    val currentMonth = LocalDate.now().monthValue

    val fullHistory: List<Pair<String, Int>> = remember(savedHistory, todaySteps) {
        val list = savedHistory.toMutableList()
        list.removeAll { it.first == todayDateStr }
        list.add(0, todayDateStr to todaySteps)
        list.sortByDescending { it.first }
        list
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF9FFF9))
        ) {
            Text(
                text = "History",
                color = Color(0xFF2E7D32),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(top = 32.dp)
                    .fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Text(
                text = "Past Steps: ...",
                color = Color(0xFF666666),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            // Milestone progress bar
            Box(
                modifier = Modifier
                    .padding(horizontal = 32.dp, vertical = 32.dp)
                    .fillMaxWidth()
                    .height(72.dp)
                    .background(Color(0xFFE8F5E8), RoundedCornerShape(36.dp))
            ) {
                val progressWidth = (currentMonth.toFloat() / 12f)
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progressWidth.coerceAtMost(1f))
                        .background(Color(0xFF4CAF50), RoundedCornerShape(36.dp))
                )

                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 36.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val months = (1..12).toList()
                    months.forEach { monthNum ->
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = monthNum.toString(),
                                color = if (monthNum == currentMonth) Color.White else Color(0xFF2E7D32),
                                fontWeight = if (monthNum == currentMonth) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 15.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // ==================== Empty text is completely, absolutely, 100% centered (pixel-aligned to your latest screenshot) ====================
            if (fullHistory.isEmpty() || (fullHistory.size == 1 && fullHistory[0].second == 0)) {
                Box(modifier = Modifier.weight(1f)) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "There was not record yet",
                            fontSize = 21.sp,
                            color = Color(0xFF666666),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "Go for a walk and help the sapling grow!",
                            fontSize = 23.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF4CAF50),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        //
                        Text(
                            text = "🌱",
                            fontSize = 56.sp,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(items = fullHistory, key = { it.first }) { item ->
                        val dateStr = item.first
                        val steps = item.second
                        if (steps == 0 && dateStr != todayDateStr) return@items

                        val localDate = LocalDate.parse(dateStr)
                        val day = localDate.dayOfMonth.toString().padStart(2, '0')
                        val monthAbbr = localDate.format(DateTimeFormatter.ofPattern("MMM.", Locale.ENGLISH))
                        val isToday = dateStr == todayDateStr

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isToday) Color(0xFF43A047) else Color(0xFF81C784)
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                            shape = RoundedCornerShape(32.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp, vertical = 20.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    modifier = Modifier.size(72.dp),
                                    shape = CircleShape,
                                    color = Color.White,
                                    shadowElevation = 8.dp
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = day,
                                            fontSize = 30.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF2E7D32)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(24.dp))

                                Column {
                                    Text(
                                        text = monthAbbr,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.White,
                                        fontSize = 18.sp
                                    )
                                    Text(
                                        text = steps.toString(),
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }

                                Spacer(modifier = Modifier.weight(1f))

                                val treeSize = when {
                                    steps >= 15000 -> 110.sp
                                    steps >= 8000 -> 95.sp
                                    steps >= 4000 -> 80.sp
                                    steps >= 1000 -> 65.sp
                                    else -> 55.sp
                                }
                                Text(text = if (steps >= 1000) "🌳" else "🌱", fontSize = treeSize)
                            }
                        }
                    }
                }
            }

            // Bottom Back button
            Button(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
                    .height(64.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                shape = RoundedCornerShape(32.dp)
            ) {
                Text(
                    text = "Back",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(text = "↑", fontSize = 32.sp)
            }
        }
    }
}