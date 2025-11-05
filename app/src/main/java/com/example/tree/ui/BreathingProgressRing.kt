package com.example.tree.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Modifier

@Composable
fun BreathingProgressRing(progress: Float) {
    val infiniteTransition = rememberInfiniteTransition()
    val breath by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    Canvas(modifier = Modifier.size(300.dp)) {
        drawCircle(color = Color.Green.copy(alpha = 0.3f), radius = size.minDimension / 2 * breath * progress)
    }
}
