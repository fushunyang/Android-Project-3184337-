package com.example.tree.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ==================== All colors are completely unified into ColorScheme. ====================
private val TreeColorScheme = lightColorScheme(
    primary       = Color(0xFF4CAF50),
    onPrimary     = Color.White,
    primaryContainer = Color(0xFF8BC34A),

    secondary     = Color(0xFF8BC34A),
    onSecondary   = Color.Black,

    tertiary      = Color(0xFF388E3C),

    background    = Color(0xFFFFFFFF),
    onBackground  = Color(0xFF1B5E20),

    surface       = Color(0xFFFFFDE7),
    onSurface     = Color(0xFF5D4037),

    surfaceVariant = Color(0xFFE8F5E9).copy(alpha = 0.95f),
    outline       = Color(0xFFE0E0E0),

)

val ColorScheme.treeSkyBlue: Color get() = Color(0xFFBBDEFB)
val ColorScheme.treeLocationCard: Color get() = Color(0xFFE3F2FD)
val ColorScheme.treeTextGood: Color get() = Color(0xFF2E7D32)
val ColorScheme.treeTextBad: Color get() = Color(0xFFD32F2F)
val ColorScheme.treeLocationIcon: Color get() = Color(0xFF1976D2)

// 背景渐变专用（Home + Status 通用）
val ColorScheme.treeBgTop: Color get() = Color(0xFFE8F5E9)
val ColorScheme.treeBgMid1: Color get() = Color(0xFFB9F6CA)
val ColorScheme.treeBgMid2: Color get() = Color(0xFFDCEDC8)
val ColorScheme.treeBgBottom: Color get() = Color(0xFFFFFFFF)

@Composable
fun TreeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = TreeColorScheme,
        typography = AppTypography,
        content = content
    )
}