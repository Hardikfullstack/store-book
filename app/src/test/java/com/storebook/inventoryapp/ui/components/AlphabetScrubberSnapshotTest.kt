package com.storebook.inventoryapp.ui.components

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [33], qualifiers = RobolectricDeviceQualifiers.Pixel5, application = android.app.Application::class)
class AlphabetScrubberSnapshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun captureAlphabetScrubber_default() {
        composeTestRule.setContent {
            AlphabetScrubber(
                onLetterSelect = {},
                modifier = Modifier.height(400.dp).padding(16.dp)
            )
        }
        
        composeTestRule.onRoot().captureRoboImage("src/test/snapshots/AlphabetScrubber_default.png")
    }
}
