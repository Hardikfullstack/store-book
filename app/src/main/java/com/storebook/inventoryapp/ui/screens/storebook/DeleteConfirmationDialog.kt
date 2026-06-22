package com.storebook.inventoryapp.ui.screens.storebook

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.storebook.inventoryapp.R
import com.storebook.inventoryapp.ui.theme.Coral500

private const val PREF_FILE = "storebook_prefs"
private const val PREF_SKIP_ITEM_DELETE = "skip_delete_item_confirm"
private const val PREF_SKIP_UDHAAR_DELETE = "skip_delete_udhaar_confirm"

/** Returns true if the user has checked "Don't show again" for inventory deletes. */
fun shouldSkipInventoryDeleteConfirm(context: Context): Boolean =
    context
        .getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
        .getBoolean(PREF_SKIP_ITEM_DELETE, false)

/** Returns true if the user has checked "Don't show again" for Udhaar entry deletes. */
fun shouldSkipUdhaarDeleteConfirm(context: Context): Boolean =
    context
        .getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
        .getBoolean(PREF_SKIP_UDHAAR_DELETE, false)

private fun persistSkipPref(
    context: Context,
    prefKey: String,
    skip: Boolean,
) {
    context
        .getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
        .edit()
        .putBoolean(prefKey, skip)
        .apply()
}

/**
 * Reusable delete confirmation dialog with a "Don't ask again" checkbox.
 *
 * - [visible]: whether to show the dialog
 * - [itemName]: quoted name shown in the message (e.g. "Sugar 1 kg")
 * - [entityLabel]: display label for the entity type (e.g. "item", "entry")
 * - [prefKey]: SharedPreferences key for the "skip" flag
 * - [onConfirm]: called when user taps Delete (after optionally persisting "skip" pref)
 * - [onDismiss]: called when user taps Cancel or dismisses
 */
@Composable
fun DeleteConfirmationDialog(
    visible: Boolean,
    itemName: String,
    entityLabel: String = "item",
    prefKey: String = PREF_SKIP_ITEM_DELETE,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    context: Context,
) {
    if (!visible) return

    var dontShowAgain by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Card(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(8.dp),
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                // Red delete icon badge
                Box(
                    modifier =
                        Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(Coral500.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = Coral500,
                        modifier = Modifier.size(30.dp),
                    )
                }

                Text(
                    text = stringResource(id = R.string.dlg_delete_entity, entityLabel),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 19.sp,
                )

                Text(
                    text = "\"$itemName\" will be moved to Trash.\nYou can recover it within 7 days.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp,
                )

                // Don't show again checkbox row
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(
                        checked = dontShowAgain,
                        onCheckedChange = { dontShowAgain = it },
                        colors =
                            CheckboxDefaults.colors(
                                checkedColor = MaterialTheme.colorScheme.primary,
                            ),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(id = R.string.dlg_dont_ask_again),
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    )
                }

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Text(
                            stringResource(id = R.string.btn_cancel),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                        )
                    }

                    Button(
                        onClick = {
                            if (dontShowAgain) {
                                persistSkipPref(context, prefKey, true)
                            }
                            onConfirm()
                            onDismiss()
                        },
                        modifier =
                            Modifier
                                .weight(1f)
                                .height(44.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onError,
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            stringResource(id = R.string.btn_delete),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onError,
                        )
                    }
                }
            }
        }
    }
}
