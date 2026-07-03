package com.storebook.inventoryapp.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.storebook.inventoryapp.ui.theme.primaryGradient

@Composable
fun DynamicFastScroller(
    listState: LazyListState,
    itemsCount: Int,
    thumbLabel: (Int) -> String,
    modifier: Modifier = Modifier
) {
    if (itemsCount == 0) return

    val coroutineScope = rememberCoroutineScope()
    var trackHeight by remember { mutableStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }
    var dragY by remember { mutableStateOf(0f) }

    // When not dragging, sync thumb position with list state
    val thumbY = remember(listState.firstVisibleItemIndex, isDragging, dragY, trackHeight, itemsCount) {
        if (isDragging) {
            dragY.coerceIn(0f, trackHeight)
        } else {
            if (itemsCount > 0) {
                // Ensure thumb doesn't exceed track boundaries
                val ratio = listState.firstVisibleItemIndex.toFloat() / itemsCount
                (ratio * trackHeight).coerceIn(0f, trackHeight)
            } else 0f
        }
    }

    val currentIndex = remember(thumbY, trackHeight, itemsCount) {
        if (trackHeight > 0) {
            ((thumbY / trackHeight) * itemsCount).toInt().coerceIn(0, itemsCount - 1)
        } else 0
    }

    Box(
        modifier = modifier
            .fillMaxHeight()
            .width(32.dp)
            .padding(vertical = 16.dp, horizontal = 4.dp)
            .onGloballyPositioned { trackHeight = it.size.height.toFloat() }
    ) {
        // Invisible touch track for dragging
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            isDragging = true
                            dragY = offset.y
                        },
                        onDragEnd = { isDragging = false },
                        onDragCancel = { isDragging = false },
                        onDrag = { change, _ ->
                            change.consume()
                            dragY = change.position.y
                            coroutineScope.launch {
                                val targetIndex = ((dragY / trackHeight) * itemsCount)
                                    .toInt()
                                    .coerceIn(0, itemsCount - 1)
                                listState.scrollToItem(targetIndex)
                            }
                        }
                    )
                }
        ) {
            // Dynamic Bubble
            AnimatedVisibility(
                visible = isDragging,
                enter = fadeIn(tween(150)),
                exit = fadeOut(tween(300)),
                modifier = Modifier
                    .graphicsLayer {
                        translationY = thumbY - 50f // Shift up slightly relative to thumb
                        translationX = -250f // Shift left to avoid covering thumb
                    }
            ) {
                Box(
                    modifier = Modifier
                        .height(36.dp)
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(MaterialTheme.primaryGradient),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = thumbLabel(currentIndex),
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1
                    )
                }
            }

            // Thumb
            Box(
                modifier = Modifier
                    .graphicsLayer {
                        translationY = thumbY
                    }
                    .size(6.dp, 48.dp) // Thumb size
                    .clip(RoundedCornerShape(3.dp))
                    .background(if (isDragging) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                    .align(Alignment.TopEnd) // Align right edge
            )
        }
    }
}
