package com.storebook.inventoryapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.storebook.inventoryapp.R
import com.storebook.inventoryapp.ui.theme.Poppins
import com.storebook.inventoryapp.ui.theme.isAppDarkMode
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.ripple
import androidx.compose.runtime.remember

@Composable
fun CustomIconButton(
    iconRes: Int,
    modifier: Modifier = Modifier,
    size: Int = 24,
    tint: Color = Color.Unspecified,
    onClick: () -> Unit,
    originalICon: Boolean = false
) {
    val isDarkMode = isAppDarkMode
    Box(
        modifier = modifier
            .clip(CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = Color.Black),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            modifier = Modifier.size(size.dp),
            contentDescription = null,
            tint = if(originalICon) tint else if (isDarkMode) Color.White else tint
        )
    }
}

@Composable
fun CustomMainButton(
    modifier: Modifier = Modifier,
    text: String = "",
    color: Color = colorResource(R.color.white),
    size: Int = 14,
    content: @Composable (() -> Unit)? = null
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        if (content != null) {
            content()
        } else {
            Text(
                text = text,
                fontFamily = Poppins,
                fontWeight = FontWeight.Medium,
                fontSize = size.sp,
                color = color
            )
        }
    }
}

@Composable
fun CustomOutlinedButton(
    text: String,
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(30.dp)
) {
    Box(
        modifier = modifier
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onSecondary,
                shape = shape
            )
            .clip(shape)
            .background(if (isAppDarkMode) Color.Transparent else colorResource(R.color.white)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontFamily = Poppins,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = if (isAppDarkMode) Color.White else colorResource(R.color.gray_text)
        )
    }
}

@Composable
fun CommonIcon(
    iconRes: Int,
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified,
    iconName: String? = null
) {
    Icon(
        painter = painterResource(id = iconRes),
        contentDescription = iconName,
        tint = tint,
        modifier = modifier
    )
}
