package com.storebook.inventoryapp.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.storebook.inventoryapp.ui.theme.primaryGradient
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

private const val LETTER_COUNT = 26
private val TRACK_WIDTH = 32.dp
private val TRACK_INNER_PADDING = 8.dp
private val PILL_SIZE = 24.dp
private val BUBBLE_SIZE = 64.dp

/**
 * Fast-scroll alphabet index.
 *
 * Design:
 *  - Touch = continuous y -> letter mapping over the FULL track (all 26 letters
 *    always reachable, snapping to the nearest letter that has data).
 *  - Visuals = adaptive. Only as many labels as physically fit are drawn
 *    (accounts for track height AND system font scale). Skipped letters are drawn
 *    as tiny dots so the rhythm of the alphabet is preserved without clutter.
 *  - One floating pill glides to the active letter (no per-row resizing => no clipping).
 *  - Labels are absolutely positioned, so they can never be cut by row bounds.
 */
@Composable
fun AlphabetScrubber(
    onLetterSelect: (Char) -> Unit,
    modifier: Modifier = Modifier,
    availableLetters: Set<Char> = ('A'..'Z').toSet(),
    topPadding: Dp = 12.dp,
    bottomPadding: Dp = 88.dp,
) {
    val alphabet = remember { ('A'..'Z').toList() }
    val density = LocalDensity.current
    val haptics = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    var isDragging by remember { mutableStateOf(false) }
    var selectedIndex by remember { mutableIntStateOf(-1) }
    var innerHeightPx by remember { mutableFloatStateOf(0f) }
    var trackTopInParentPx by remember { mutableFloatStateOf(0f) }

    val paddingPx = with(density) { TRACK_INNER_PADDING.toPx() }

    fun centerYPx(index: Int): Float = (index + 0.5f) / LETTER_COUNT * innerHeightPx

    // Animated Y of the active letter (inner-track coordinates).
    val activeY = remember { Animatable(0f) }

    fun nearestAvailableIndex(raw: Int): Int {
        val clamped = raw.coerceIn(0, alphabet.lastIndex)
        if (alphabet[clamped] in availableLetters) return clamped
        for (delta in 1..alphabet.size) {
            val down = clamped - delta
            val up = clamped + delta
            if (down >= 0 && alphabet[down] in availableLetters) return down
            if (up <= alphabet.lastIndex && alphabet[up] in availableLetters) return up
        }
        return clamped
    }

    fun indexForY(y: Float): Int {
        if (innerHeightPx <= 0f) return -1
        val raw = (y / innerHeightPx * LETTER_COUNT).toInt()
        return nearestAvailableIndex(raw)
    }

    fun selectIndex(
        index: Int,
        snap: Boolean,
    ) {
        if (index == -1) return
        if (index == selectedIndex && !snap) return
        selectedIndex = index
        val target = centerYPx(index)
        scope.launch {
            if (snap) {
                activeY.snapTo(target)
            } else {
                activeY.animateTo(
                    target,
                    spring(Spring.DampingRatioLowBouncy, Spring.StiffnessMedium),
                )
            }
        }
        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        onLetterSelect(alphabet[index])
    }

    // ---------- Adaptive label layout ----------
    val labelFontSp = 11.sp
    val labelHeightDp = with(density) { labelFontSp.toDp() } + 7.dp // font-scale aware slot
    val innerHeightDp = with(density) { innerHeightPx.toDp() }
    val maxLabels = (innerHeightDp / labelHeightDp).toInt()

    val labelIndices: List<Int> =
        remember(maxLabels) {
            when {
                maxLabels >= LETTER_COUNT -> (0 until LETTER_COUNT).toList()
                maxLabels < 2 -> emptyList()
                else ->
                    (0 until maxLabels)
                        .map { k -> (k * (LETTER_COUNT - 1) / (maxLabels - 1f)).roundToInt() }
                        .distinct()
            }
        }
    val letterPitchDp = if (innerHeightPx > 0f) innerHeightDp / LETTER_COUNT else 0.dp

    val trackScale by animateFloatAsState(
        targetValue = if (isDragging) 1.05f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "trackScale",
    )

    Box(modifier = modifier) {
        // ---------- Floating preview bubble ----------
        AnimatedVisibility(
            visible = isDragging && selectedIndex >= 0,
            enter = fadeIn(tween(120)) + scaleIn(tween(120), initialScale = 0.7f),
            exit = fadeOut(tween(200)) + scaleOut(tween(200), targetScale = 0.7f),
            modifier =
                Modifier
                    .align(Alignment.TopStart)
                    .offset {
                        val bubbleHalf = BUBBLE_SIZE.toPx() / 2f
                        val y =
                            (trackTopInParentPx + paddingPx + activeY.value - bubbleHalf)
                                .coerceAtLeast(0f) // never off the top edge
                        IntOffset((-(BUBBLE_SIZE + 12.dp).toPx()).roundToInt(), y.roundToInt())
                    },
        ) {
            Box(
                modifier =
                    Modifier
                        .size(BUBBLE_SIZE)
                        .shadow(8.dp, CircleShape, clip = false)
                        .clip(CircleShape)
                        .background(MaterialTheme.primaryGradient),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (selectedIndex >= 0) alphabet[selectedIndex].toString() else "",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        // ---------- Track ----------
        Box(
            modifier =
                Modifier
                    .width(TRACK_WIDTH)
                    .fillMaxHeight()
                    .padding(top = topPadding, bottom = bottomPadding)
                    .align(Alignment.CenterEnd)
                    .onGloballyPositioned { trackTopInParentPx = it.positionInParent().y }
                    .graphicsLayer {
                        scaleX = trackScale
                        scaleY = trackScale
                    }.shadow(
                        elevation = if (isDragging) 4.dp else 0.dp,
                        shape = RoundedCornerShape(TRACK_WIDTH / 2),
                        clip = false,
                    ).background(
                        MaterialTheme.colorScheme.surface.copy(alpha = if (isDragging) 0.96f else 0.72f),
                        RoundedCornerShape(TRACK_WIDTH / 2),
                    ).padding(vertical = TRACK_INNER_PADDING)
                    .semantics { contentDescription = "Alphabet index. Drag up or down to jump to a letter" },
        ) {
            // Inner area: exact coordinate space for both touch and drawing.
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .onGloballyPositioned { innerHeightPx = it.size.height.toFloat() }
                        .pointerInput(availableLetters) {
                            awaitEachGesture {
                                val down = awaitFirstDown(pass = PointerEventPass.Initial)
                                down.consume()
                                isDragging = true
                                selectedIndex = -1
                                selectIndex(indexForY(down.position.y), snap = true)

                                while (true) {
                                    val event = awaitPointerEvent(pass = PointerEventPass.Initial)
                                    val change = event.changes.firstOrNull() ?: break
                                    if (!change.pressed) {
                                        change.consume()
                                        break
                                    }
                                    if (change.positionChange() != Offset.Zero) {
                                        change.consume()
                                        selectIndex(indexForY(change.position.y), snap = false)
                                    }
                                }
                                isDragging = false
                                selectedIndex = -1
                            }
                        },
            ) {
                // Dots for letters that have no label (only where there is room).
                if (labelIndices.size < LETTER_COUNT && labelIndices.isNotEmpty()) {
                    for (i in 0 until LETTER_COUNT) {
                        if (i in labelIndices) continue
                        val nearestLabelGap = labelIndices.minOf { abs(it - i) }
                        if (letterPitchDp * nearestLabelGap < 10.dp) continue
                        val hasData = alphabet[i] in availableLetters
                        Box(
                            modifier =
                                Modifier
                                    .align(Alignment.TopCenter)
                                    .offset { IntOffset(0, (centerYPx(i) - 1.5.dp.toPx()).roundToInt()) }
                                    .size(3.dp)
                                    .clip(CircleShape)
                                    .background(
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                            .copy(alpha = if (hasData) 0.55f else 0.2f),
                                    ),
                        )
                    }
                }

                // Active pill: ONE element gliding along the track.
                if (isDragging && selectedIndex >= 0) {
                    Box(
                        modifier =
                            Modifier
                                .align(Alignment.TopCenter)
                                .offset { IntOffset(0, (activeY.value - PILL_SIZE.toPx() / 2f).roundToInt()) }
                                .size(PILL_SIZE)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = alphabet[selectedIndex].toString(),
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }

                // Labels, absolutely positioned so nothing can be clipped by a row.
                labelIndices.forEach { i ->
                    val hasData = alphabet[i] in availableLetters
                    // Hide a label while the pill is covering it.
                    val coveredByPill =
                        isDragging &&
                            selectedIndex >= 0 &&
                            abs(centerYPx(i) - activeY.value) < with(density) { (PILL_SIZE * 0.75f).toPx() }
                    Box(
                        modifier =
                            Modifier
                                .align(Alignment.TopCenter)
                                .offset { IntOffset(0, (centerYPx(i) - labelHeightDp.toPx() / 2f).roundToInt()) }
                                .size(width = TRACK_WIDTH, height = labelHeightDp)
                                .alpha(if (coveredByPill) 0f else 1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = alphabet[i].toString(),
                            modifier = Modifier.wrapContentSize(unbounded = true),
                            fontSize = labelFontSp,
                            fontWeight = FontWeight.SemiBold,
                            color =
                                MaterialTheme.colorScheme.onSurfaceVariant
                                    .copy(alpha = if (hasData) 0.85f else 0.3f),
                        )
                    }
                }
            }
        }
    }
}
