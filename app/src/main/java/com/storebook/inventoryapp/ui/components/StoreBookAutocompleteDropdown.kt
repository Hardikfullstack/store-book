package com.storebook.inventoryapp.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import com.storebook.inventoryapp.utils.autoMarquee

class ImeAwareDropdownPositionProvider(
    private val imeBottomPx: Int,
    private val forceAbove: Boolean? = null,
    private val verticalGapPx: Int = 8,
) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset {
        val x = minOf(anchorBounds.left, maxOf(0, windowSize.width - popupContentSize.width))
        val visibleBottom = windowSize.height - imeBottomPx
        val spaceBelow = visibleBottom - anchorBounds.bottom
        val spaceAbove = anchorBounds.top

        val shouldOpenAbove =
            when (forceAbove) {
                true -> true
                false -> false
                null -> (spaceBelow < popupContentSize.height && spaceAbove > spaceBelow) || imeBottomPx > 0
            }

        val y =
            if (shouldOpenAbove) {
                maxOf(0, anchorBounds.top - popupContentSize.height - verticalGapPx)
            } else {
                anchorBounds.bottom + verticalGapPx
            }

        return IntOffset(x, y)
    }
}

@Composable
fun <T> StoreBookAutocompleteDropdown(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    suggestions: List<T>,
    itemText: (T) -> String,
    onSuggestionSelected: (T) -> Unit,
    avatarColor: Color,
    avatarTextColor: Color,
    modifier: Modifier = Modifier,
    openAbove: Boolean? = null,
    additionalContent: @Composable ((T) -> Unit)? = null,
) {
    if (!expanded || suggestions.isEmpty()) return

    val density = LocalDensity.current
    val imeBottom = WindowInsets.ime.getBottom(density)
    val verticalGapPx = with(density) { 4.dp.roundToPx() }

    val positionProvider =
        remember(imeBottom, openAbove, verticalGapPx) {
            ImeAwareDropdownPositionProvider(
                imeBottomPx = imeBottom,
                forceAbove = openAbove,
                verticalGapPx = verticalGapPx,
            )
        }

    Popup(
        onDismissRequest = onDismissRequest,
        popupPositionProvider = positionProvider,
        properties =
            PopupProperties(
                focusable = false,
                dismissOnBackPress = true,
                dismissOnClickOutside = true,
            ),
    ) {
        Card(
            modifier = modifier.heightIn(max = 220.dp),
            shape = RoundedCornerShape(12.dp),
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
            ) {
                suggestions.take(5).forEach { item ->
                    val text = itemText(item)
                    val initial = if (text.isNotBlank()) text.take(1).uppercase() else "?"
                    DropdownMenuItem(
                        text = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(
                                    modifier =
                                        Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(avatarColor),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = initial,
                                        color = avatarTextColor,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = text,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 2,
                                        modifier = Modifier.autoMarquee(),
                                    )
                                    additionalContent?.invoke(item)
                                }
                            }
                        },
                        onClick = { onSuggestionSelected(item) },
                    )
                }
            }
        }
    }
}
