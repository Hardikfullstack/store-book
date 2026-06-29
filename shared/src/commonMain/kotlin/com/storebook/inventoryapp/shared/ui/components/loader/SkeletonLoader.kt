package com.storebook.inventoryapp.shared.ui.components.loader

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

@Composable
fun SkeletonLoader(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    count: Int = 3,
    content: @Composable () -> Unit,
) {
    if (isLoading) {
        Column(modifier = modifier) {
            repeat(count) {
                SkeletonItem()
            }
        }
    } else {
        content()
    }
}

@Composable
fun SkeletonItem() {
    val isDark = isSystemInDarkTheme()
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp)
                .background(
                    color = if (isDark) Color(0xFF212121) else MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(10.dp),
                ).padding(12.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .size(40.dp)
                    .shimmerEffect(),
        )
        Spacer(
            modifier =
                Modifier
                    .width(12.dp),
        )
        Column(
            modifier =
                Modifier
                    .weight(1f),
        ) {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth(0.6f)
                        .height(16.dp)
                        .shimmerEffect(),
            )
            Spacer(
                modifier =
                    Modifier
                        .height(8.dp),
            )
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth(0.4f)
                        .height(12.dp)
                        .shimmerEffect(),
            )
        }
    }
}

@Composable
fun TextSkeletonItem() {
    val isDark = isSystemInDarkTheme()
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .background(
                    color = if (isDark) Color(0xFF212121) else MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(10.dp),
                ).padding(16.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth(0.7f)
                    .height(28.dp)
                    .shimmerEffect(),
        )
        Spacer(
            modifier =
                Modifier
                    .height(16.dp),
        )
        Box(
            modifier =
                Modifier
                    .fillMaxWidth(0.8f)
                    .height(14.dp)
                    .shimmerEffect(),
        )
        Spacer(modifier = Modifier.height(10.dp))
        Box(
            modifier =
                Modifier
                    .fillMaxWidth(0.9f)
                    .height(12.dp)
                    .shimmerEffect(),
        )
        Spacer(modifier = Modifier.height(10.dp))
        Box(
            modifier =
                Modifier
                    .fillMaxWidth(0.9f)
                    .height(12.dp)
                    .shimmerEffect(),
        )
        Spacer(modifier = Modifier.height(10.dp))
        Box(
            modifier =
                Modifier
                    .fillMaxWidth(0.9f)
                    .height(12.dp)
                    .shimmerEffect(),
        )
        Spacer(modifier = Modifier.height(10.dp))
        Box(
            modifier =
                Modifier
                    .fillMaxWidth(0.9f)
                    .height(12.dp)
                    .shimmerEffect(),
        )
        Spacer(modifier = Modifier.height(10.dp))
        Box(
            modifier =
                Modifier
                    .fillMaxWidth(0.9f)
                    .height(12.dp)
                    .shimmerEffect(),
        )
    }
}

fun Modifier.shimmerEffect(shape: Shape = RoundedCornerShape(4.dp)): Modifier =
    composed {
        val transition = rememberInfiniteTransition(label = "shimmer")
        val translateAnim =
            transition.animateFloat(
                initialValue = 0f,
                targetValue = 1000f,
                animationSpec =
                    infiniteRepeatable(
                        animation = tween(durationMillis = 1000, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart,
                    ),
                label = "shimmer",
            )

        val shimmerColors =
            listOf(
                Color.LightGray.copy(alpha = 0.6f),
                Color.LightGray.copy(alpha = 0.2f),
                Color.LightGray.copy(alpha = 0.6f),
            )

        this.drawWithContent {
            drawContent()

            val translateValue = translateAnim.value
            val brush =
                Brush.linearGradient(
                    colors = shimmerColors,
                    start = Offset.Zero,
                    end = Offset(x = translateValue, y = translateValue),
                )

            val outline = shape.createOutline(size, layoutDirection, this)
            if (outline is androidx.compose.ui.graphics.Outline.Rounded) {
                drawRoundRect(
                    brush = brush,
                    cornerRadius = outline.roundRect.topLeftCornerRadius,
                    size = size,
                )
            } else if (outline is androidx.compose.ui.graphics.Outline.Rectangle) {
                drawRect(brush = brush, size = size)
            } else if (outline is androidx.compose.ui.graphics.Outline.Generic) {
                drawPath(path = outline.path, brush = brush)
            }
        }
    }
