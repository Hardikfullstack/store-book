package com.storebook.inventoryapp.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AlphabetScrubber(
    onLetterSelect: (Char) -> Unit,
    modifier: Modifier = Modifier
) {
    val alphabet = ('A'..'Z').toList()
    var selectedLetter by remember { mutableStateOf<Char?>(null) }
    var columnHeight by remember { mutableStateOf(0f) }
    
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        // Floating Bubble
        AnimatedVisibility(
            visible = selectedLetter != null,
            enter = fadeIn(tween(150)),
            exit = fadeOut(tween(300)),
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = (-70).dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = selectedLetter?.toString() ?: "",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // The Scrubber Column
        Column(
            modifier = Modifier
                .width(28.dp)
                .fillMaxHeight(0.85f) // Increased to 85% of screen height
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f), CircleShape)
                .padding(vertical = 8.dp)
                .onGloballyPositioned { coords ->
                    columnHeight = coords.size.height.toFloat()
                }
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onDragStart = { offset ->
                            if (columnHeight > 0) {
                                val itemHeight = columnHeight / alphabet.size
                                val index = (offset.y / itemHeight).toInt().coerceIn(0, alphabet.lastIndex)
                                selectedLetter = alphabet[index]
                                onLetterSelect(alphabet[index])
                            }
                        },
                        onDragEnd = { selectedLetter = null },
                        onDragCancel = { selectedLetter = null },
                        onVerticalDrag = { change, _ ->
                            if (columnHeight > 0) {
                                val y = change.position.y
                                val itemHeight = columnHeight / alphabet.size
                                val index = (y / itemHeight).toInt().coerceIn(0, alphabet.lastIndex)
                                if (selectedLetter != alphabet[index]) {
                                    selectedLetter = alphabet[index]
                                    onLetterSelect(alphabet[index])
                                }
                            }
                        }
                    )
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            alphabet.forEach { letter ->
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = letter.toString(),
                        fontSize = 10.sp,
                        fontWeight = if (selectedLetter == letter) FontWeight.Bold else FontWeight.Medium,
                        color = if (selectedLetter == letter) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
