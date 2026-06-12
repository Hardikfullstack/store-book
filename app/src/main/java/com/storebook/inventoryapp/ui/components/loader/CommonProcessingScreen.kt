package com.storebook.inventoryapp.ui.components.loader

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.storebook.inventoryapp.R
import com.storebook.inventoryapp.ui.theme.Poppins
import com.storebook.inventoryapp.ui.theme.isAppDarkMode

@Composable
fun CommonProcessingScreen(
    progress: Int,
    lottieRes: Int? = null,
    title: String,
    isCompressed: Boolean = false,
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress / 100f,
        animationSpec = tween(durationMillis = 500),
        label = "ProgressAnimation",
    )

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.primary),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (lottieRes != null) {
            val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(lottieRes))
            LottieAnimation(
                composition = composition,
                modifier = Modifier.size(200.dp).scale(if (isCompressed) 1f else 2f),
                iterations = LottieConstants.IterateForever,
            )
            Spacer(modifier = Modifier.height(0.dp))
        }
        Text(
            text = "${(animatedProgress * 100).toInt()}%",
            fontSize = 30.sp,
            fontFamily = Poppins,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.secondary,
        )

        Spacer(modifier = Modifier.height(8.dp))

        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier =
                Modifier
                    .fillMaxWidth(0.6f)
                    .height(4.dp)
                    .clip(RoundedCornerShape(50)),
            color = colorResource(R.color.red_text),
            trackColor = if (isAppDarkMode) Color(0xFF333333) else Color(0xFFD9D9D9),
            drawStopIndicator = {},
            gapSize = 0.dp,
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = title,
            fontSize = 20.sp,
            fontFamily = Poppins,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.secondary,
        )
    }
}
