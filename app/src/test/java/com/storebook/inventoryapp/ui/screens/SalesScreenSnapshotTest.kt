package com.storebook.inventoryapp.ui.screens

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.navigation.NavController
import androidx.test.core.app.ApplicationProvider
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.storebook.inventoryapp.shared.domain.models.*
import com.storebook.inventoryapp.ui.screens.storebook.SalesScreen
import com.storebook.inventoryapp.ui.theme.LocalAppTheme
import com.storebook.inventoryapp.ui.theme.ManualThemeManager
import com.storebook.inventoryapp.ui.theme.StoreBookTheme
import com.storebook.inventoryapp.ui.viewmodel.SalesViewModel
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
class SalesScreenSnapshotTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var mockSalesViewModel: SalesViewModel
    private lateinit var navController: NavController
    private lateinit var themeManager: ManualThemeManager

    @Before
    fun setup() {
        mockkStatic(FirebaseAuth::class)
        val mockAuth = mockk<FirebaseAuth>(relaxed = true)
        val mockUser = mockk<FirebaseUser>(relaxed = true)
        every { mockUser.phoneNumber } returns "+919876543210"
        every { mockAuth.currentUser } returns mockUser
        every { FirebaseAuth.getInstance() } returns mockAuth

        mockkStatic(com.google.mlkit.vision.codescanner.GmsBarcodeScanning::class)
        val mockScanner = mockk<com.google.mlkit.vision.codescanner.GmsBarcodeScanner>(relaxed = true)
        every {
            com.google.mlkit.vision.codescanner.GmsBarcodeScanning
                .getClient(
                    any<android.content.Context>(),
                    any<com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions>(),
                )
        } returns mockScanner

        mockSalesViewModel = mockk(relaxed = true)
        navController = mockk(relaxed = true)
        themeManager = ManualThemeManager(ApplicationProvider.getApplicationContext())

        // Setup base flows for SalesViewModel
        every { mockSalesViewModel.allItems } returns MutableStateFlow(emptyList<Item>())
        every { mockSalesViewModel.udhaarBalances } returns MutableStateFlow(emptyList<CustomerBalance>())
        every { mockSalesViewModel.cartItems } returns mutableListOf()
    }

    @Test
    @Config(qualifiers = RobolectricDeviceQualifiers.Pixel5)
    fun captureSalesScreen_default_light() {
        composeTestRule.setContent {
            CompositionLocalProvider(LocalAppTheme provides themeManager) {
                StoreBookTheme(darkTheme = false) {
                    SalesScreen(
                        navController = navController,
                        viewModel = mockSalesViewModel,
                    )
                }
            }
        }
        composeTestRule.onRoot().captureRoboImage("src/test/snapshots/SalesScreen_Phone_Light.png")
    }

    @Test
    @Config(qualifiers = RobolectricDeviceQualifiers.Pixel5)
    fun captureSalesScreen_default_dark() {
        composeTestRule.setContent {
            CompositionLocalProvider(LocalAppTheme provides themeManager) {
                StoreBookTheme(darkTheme = true) {
                    SalesScreen(
                        navController = navController,
                        viewModel = mockSalesViewModel,
                    )
                }
            }
        }
        composeTestRule.onRoot().captureRoboImage("src/test/snapshots/SalesScreen_Phone_Dark.png")
    }

    @Test
    @Config(qualifiers = "w1280dp-h800dp")
    fun captureSalesScreen_tablet_layout() {
        composeTestRule.setContent {
            CompositionLocalProvider(LocalAppTheme provides themeManager) {
                StoreBookTheme(darkTheme = false) {
                    SalesScreen(
                        navController = navController,
                        viewModel = mockSalesViewModel,
                    )
                }
            }
        }
        composeTestRule.onRoot().captureRoboImage("src/test/snapshots/SalesScreen_Tablet.png")
    }
}
