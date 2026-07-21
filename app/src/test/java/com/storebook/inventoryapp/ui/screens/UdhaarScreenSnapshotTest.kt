package com.storebook.inventoryapp.ui.screens

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.core.app.ApplicationProvider
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.storebook.inventoryapp.shared.domain.models.*
import com.storebook.inventoryapp.ui.screens.storebook.UdhaarScreen
import com.storebook.inventoryapp.ui.theme.LocalAppTheme
import com.storebook.inventoryapp.ui.theme.ManualThemeManager
import com.storebook.inventoryapp.ui.theme.StoreBookTheme
import com.storebook.inventoryapp.ui.viewmodel.UdhaarViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [33], application = android.app.Application::class)
class UdhaarScreenSnapshotTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var mockUdhaarViewModel: UdhaarViewModel
    private lateinit var themeManager: ManualThemeManager

    @Before
    fun setup() {
        mockkStatic(FirebaseAuth::class)
        val mockAuth = mockk<FirebaseAuth>(relaxed = true)
        val mockUser = mockk<FirebaseUser>(relaxed = true)
        every { mockUser.phoneNumber } returns "+919876543210"
        every { mockAuth.currentUser } returns mockUser
        every { FirebaseAuth.getInstance() } returns mockAuth

        mockUdhaarViewModel = mockk(relaxed = true)
        themeManager = ManualThemeManager(ApplicationProvider.getApplicationContext())

        every { mockUdhaarViewModel.udhaarEntries } returns MutableStateFlow(emptyList<UdhaarEntry>())
        every { mockUdhaarViewModel.udhaarBalances } returns MutableStateFlow(emptyList<CustomerBalance>())
        every { mockUdhaarViewModel.detailedBalances } returns MutableStateFlow(emptyList<CustomerDetailedBalance>())
        every { mockUdhaarViewModel.errorMessage } returns MutableStateFlow(null)
    }

    @Test
    @Config(qualifiers = RobolectricDeviceQualifiers.Pixel5)
    fun captureUdhaarScreen_default_light() {
        composeTestRule.setContent {
            CompositionLocalProvider(LocalAppTheme provides themeManager) {
                StoreBookTheme(darkTheme = false) {
                    UdhaarScreen(
                        viewModel = mockUdhaarViewModel,
                    )
                }
            }
        }
        composeTestRule.onRoot().captureRoboImage("src/test/snapshots/UdhaarScreen_Phone_Light.png")
    }

    @Test
    @Config(qualifiers = RobolectricDeviceQualifiers.Pixel5)
    fun captureUdhaarScreen_default_dark() {
        composeTestRule.setContent {
            CompositionLocalProvider(LocalAppTheme provides themeManager) {
                StoreBookTheme(darkTheme = true) {
                    UdhaarScreen(
                        viewModel = mockUdhaarViewModel,
                    )
                }
            }
        }
        composeTestRule.onRoot().captureRoboImage("src/test/snapshots/UdhaarScreen_Phone_Dark.png")
    }
}
