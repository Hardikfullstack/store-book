package com.storebook.inventoryapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import com.storebook.inventoryapp.utils.autoMarquee

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
    additionalContent: @Composable ((T) -> Unit)? = null
) {
    DropdownMenu(
        expanded = expanded && suggestions.isNotEmpty(),
        onDismissRequest = onDismissRequest,
        properties = PopupProperties(focusable = false),
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface)
    ) {
        suggestions.take(5).forEach { item ->
            val text = itemText(item)
            val initial = if (text.isNotBlank()) text.take(1).uppercase() else "?"
            DropdownMenuItem(
                text = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(avatarColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = initial,
                                color = avatarTextColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
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
                                modifier = Modifier.autoMarquee()
                            )
                            additionalContent?.invoke(item)
                        }
                    }
                },
                onClick = { onSuggestionSelected(item) }
            )
        }
    }
}
