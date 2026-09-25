package com.storebook.inventoryapp.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.storebook.inventoryapp.ui.theme.primaryGradient

@Composable
fun AlphabetScrubber(
    onLetterSelect: (Char) -> Unit,
    modifier: Modifier = Modifier,
) {
    val alphabet = ('A'..'Z').toList()
    var selectedLetter by remember { mutableStateOf<Char?>(null) }
    var columnHeight by remember { mutableStateOf(0f) }

    fun letterForY(y: Float): Char {
        val itemHeight = columnHeight / alphabet.size
        val index = (y / itemHeight).toInt().coerceIn(0, alphabet.lastIndex)
        return alphabet[index]
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        AnimatedVisibility(
            visible = selectedLetter != null,
            enter = fadeIn(tween(150)),
            exit = fadeOut(tween(300)),
            modifier = Modifier.align(Alignment.CenterStart).offset(x = (-70).dp),
        ) {
            Box(
                modifier =
                    Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.primaryGradient),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = selectedLetter?.toString() ?: "",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        Column(
            modifier =
                Modifier
                    .width(28.dp)
                    .fillMaxHeight(0.9f)
                    .background(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                        CircleShape,
                    ).padding(vertical = 4.dp)
                    .onGloballyPositioned { coords ->
                        columnHeight = coords.size.height.toFloat()
                    }
                    // Claim every pointer event on this column at the INITIAL pass,
                    // before the parent LazyColumn's scroll gesture ever sees it.
                    .pointerInput(Unit) {
                        awaitEachGesture {
                            val down = awaitFirstDown(pass = PointerEventPass.Initial)
                            down.consume() // steal the touch from the parent list immediately

                            if (columnHeight > 0f) {
                                val letter = letterForY(down.position.y)
                                selectedLetter = letter
                                onLetterSelect(letter)
                            }

                            // Track drag until finger lifts
                            while (true) {
                                val event = awaitPointerEvent(pass = PointerEventPass.Initial)
                                val change = event.changes.firstOrNull() ?: break

                                if (!change.pressed) {
                                    // finger lifted
                                    change.consume()
                                    break
                                }

                                if (change.positionChange() != androidx.compose.ui.geometry.Offset.Zero) {
                                    change.consume()
                                    if (columnHeight > 0f) {
                                        val letter = letterForY(change.position.y)
                                        if (letter != selectedLetter) {
                                            selectedLetter = letter
                                            onLetterSelect(letter)
                                        }
                                    }
                                }
                            }
                            selectedLetter = null
                        }
                    },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            alphabet.forEach { letter ->
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = letter.toString(),
                        fontSize = 7.sp,
                        fontWeight = if (selectedLetter == letter) FontWeight.Bold else FontWeight.Medium,
                        color =
                            if (selectedLetter == letter) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                    )
                }
            }
        }
    }
}
