package com.storebook.inventoryapp.utils

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max

@Composable
fun getCompactTopPadding() =
    run {
        val cutoutTop = WindowInsets.displayCutout.asPaddingValues().calculateTopPadding()
        val statusTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
        if (cutoutTop > 0.dp) {
            cutoutTop + 2.dp
        } else {
            max(24.dp, statusTop - 8.dp)
        }
    }

fun Modifier.compactTopPadding() =
    composed {
        this.padding(top = getCompactTopPadding())
    }

val compactWindowInsets: WindowInsets
    @Composable
    get() = WindowInsets(top = getCompactTopPadding())
