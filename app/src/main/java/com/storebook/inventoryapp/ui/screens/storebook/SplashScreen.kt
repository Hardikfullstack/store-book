package com.storebook.inventoryapp.ui.screens.storebook

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.DisposableEffect
import android.app.Activity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.storebook.inventoryapp.R
import com.storebook.inventoryapp.ui.theme.Poppins
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onSplashFinished: () -> Unit) {
    val context = LocalContext.current
    val window = (context as? Activity)?.window
    if (window != null) {
        DisposableEffect(Unit) {
            val insetsController = WindowCompat.getInsetsController(window, window.decorView)
            insetsController.hide(WindowInsetsCompat.Type.systemBars())
            insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            onDispose {
                insetsController.show(WindowInsetsCompat.Type.statusBars())
            }
        }
    }

    // ── Animation triggers ────────────────────────────────────────────────
    var started by remember { mutableStateOf(false) }

    val logoAlpha by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label = "logoAlpha",
    )
    val logoScale by animateFloatAsState(
        targetValue = if (started) 1f else 0.55f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow),
        label = "logoScale",
    )
    val nameAlpha by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(700, delayMillis = 500, easing = FastOutSlowInEasing),
        label = "nameAlpha",
    )
    val tagAlpha by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(700, delayMillis = 820, easing = FastOutSlowInEasing),
        label = "tagAlpha",
    )

    // Logo breathing pulse
    val pulse by rememberInfiniteTransition(label = "pulse").animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(tween(1400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse",
    )

    // Gold shimmer sweep
    val shimmer by rememberInfiniteTransition(label = "shimmer").animateFloat(
        initialValue = -400f,
        targetValue = 900f,
        animationSpec = infiniteRepeatable(tween(1800, easing = LinearEasing), RepeatMode.Restart),
        label = "shimmer",
    )

    LaunchedEffect(Unit) {
        started = true
        delay(4000)
        onSplashFinished()
    }

    // ── Brand Colors ──────────────────────────────────────────────────────
    val bgColor = Color(0xFF191958)
    val indigoPurple = Color(0xFF6366F1)
    val goldMain     = Color(0xFFF59E0B)
    val goldLight    = Color(0xFFFBBF24)

    // ── Screen ────────────────────────────────────────────────────────────
    Box(
        modifier = Modifier.fillMaxSize().background(bgColor),
        contentAlignment = Alignment.Center,
    ) {

        // Background glow
        Canvas(Modifier.size(300.dp)) {
            drawCircle(
                Brush.radialGradient(
                    listOf(indigoPurple.copy(alpha = 0.22f * logoAlpha), Color.Transparent)
                ),
                radius = size.minDimension / 1.5f,
            )
        }

        // Centre content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {

            // ── Logo ──────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .scale(logoScale * pulse)
                    .alpha(logoAlpha),
                contentAlignment = Alignment.Center,
            ) {
                // Gold glow ring behind logo
                Canvas(Modifier.fillMaxSize()) {
                    drawCircle(
                        Brush.radialGradient(
                            listOf(goldMain.copy(alpha = 0.22f), Color.Transparent)
                        ),
                        radius = size.minDimension / 1.7f,
                    )
                }
                // logo.webp image
                Image(
                    painter = painterResource(R.drawable.logo),
                    contentDescription = "StoreBook Logo",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(130.dp)
                        .clip(RoundedCornerShape(22.dp)),
                )
            }

            Spacer(Modifier.height(28.dp))

            // ── App name ──────────────────────────────────────────────────
            Text(
                text = "StoreBook",
                fontFamily = Poppins,
                fontWeight = FontWeight.Bold,
                fontSize = 38.sp,
                color = Color.White,
                textAlign = TextAlign.Center,
                letterSpacing = 0.5.sp,
                modifier = Modifier.alpha(nameAlpha),
            )

            Spacer(Modifier.height(6.dp))

            // ── Tagline ───────────────────────────────────────────────────
            Text(
                text = "Business Leader",
                fontFamily = Poppins,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                color = Color(0xFFBFBFFF),
                textAlign = TextAlign.Center,
                letterSpacing = 0.3.sp,
                modifier = Modifier.alpha(tagAlpha),
            )

            Spacer(Modifier.height(12.dp))

            // ── Shimmer gold divider ──────────────────────────────────────
            Box(
                modifier = Modifier
                    .alpha(tagAlpha)
                    .width(110.dp)
                    .height(2.dp),
            ) {
                Canvas(Modifier.fillMaxSize()) {
                    drawRect(
                        Brush.linearGradient(
                            listOf(
                                Color.Transparent,
                                goldMain.copy(alpha = 0.4f),
                                goldLight,
                                goldMain.copy(alpha = 0.4f),
                                Color.Transparent,
                            ),
                            start = Offset(shimmer - 200f, 0f),
                            end = Offset(shimmer + 200f, 0f),
                        ),
                        size = size,
                    )
                }
            }
        }

        // ── Bottom badge ──────────────────────────────────────────────────
        Text(
            text = "🇮🇳  Made for Indian Businesses",
            fontFamily = Poppins,
            fontWeight = FontWeight.Normal,
            fontSize = 11.sp,
            color = Color(0xFF818CF8).copy(alpha = 0.8f),
            textAlign = TextAlign.Center,
            letterSpacing = 0.2.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 44.dp)
                .alpha(tagAlpha),
        )
    }
}

