package com.example.tree.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DirectionsWalk
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
import androidx.datastore.preferences.core.edit
import androidx.navigation.NavController
import com.example.tree.data.UserPreferences
import com.example.tree.data.dataStore
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // 实时读取当前目标步数
    val currentGoal by UserPreferences.goalFlow(context).collectAsState(initial = 10000)
    var goalText by remember(currentGoal) { mutableStateOf(currentGoal.toString()) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFFDF7)) // 与你截图完全一致的浅米色背景
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

                // 输入框（完全复制你截图的样式）
                OutlinedTextField(
                    value = goalText,
                    onValueChange = { input ->
                        // 只允许数字输入
                        if (input.isEmpty() || input.all { it.isDigit() }) {
                            goalText = input
                        }
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.DirectionsWalk,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50)
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
                        focusedBorderColor = Color(0xFF4CAF50),
                        unfocusedBorderColor = Color(0xFFE0E0E0),
                        cursorColor = Color(0xFF4CAF50)
                    )
                )

                Spacer(modifier = Modifier.height(48.dp))

                // Save Goal 绿色按钮（带 ✓ 图标）
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
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
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

                // Reset Tree 按钮（彻底修复 null 问题 + 完美重置逻辑）
                Button(
                    onClick = {
                        coroutineScope.launch {
                            context.dataStore.edit { prefs ->
                                // 1. 当天步数立刻清零 → 树马上变回小苗
                                prefs[UserPreferences.DAILY_STEPS_KEY] = 0
                                // 2. 删除累计基数和日期记录
                                //    下次走路时会以当前传感器值作为新起点，完美从0开始
                                prefs.remove(UserPreferences.LAST_TOTAL_STEPS_KEY)
                                prefs.remove(UserPreferences.LAST_DATE_KEY)
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
                        color = Color(0xFF666666),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Back to Home（位置与截图完全一致）
        Text(
            text = "Back to Home",
            fontSize = 17.sp,
            color = Color(0xFF666666),
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