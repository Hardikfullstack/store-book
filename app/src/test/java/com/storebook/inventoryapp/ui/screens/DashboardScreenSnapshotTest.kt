package com.storebook.inventoryapp.ui.screens

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.Density
import androidx.navigation.NavController
import androidx.test.core.app.ApplicationProvider
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.storebook.inventoryapp.shared.domain.models.*
import com.storebook.inventoryapp.ui.screens.storebook.DashboardScreen
import com.storebook.inventoryapp.ui.theme.LocalAppTheme
import com.storebook.inventoryapp.ui.theme.ManualThemeManager
import com.storebook.inventoryapp.ui.theme.StoreBookTheme
import com.storebook.inventoryapp.ui.viewmodel.DashboardViewModel
import com.storebook.inventoryapp.ui.viewmodel.SalesViewModel
import com.storebook.inventoryapp.ui.viewmodel.UiSyncStatus
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
class DashboardScreenSnapshotTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var mockDashboardViewModel: DashboardViewModel
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

        mockDashboardViewModel = mockk(relaxed = true)
        mockSalesViewModel = mockk(relaxed = true)
        navController = mockk(relaxed = true)
        themeManager = ManualThemeManager(ApplicationProvider.getApplicationContext())

        // Setup base flows for DashboardViewModel
        every { mockDashboardViewModel.allItems } returns MutableStateFlow(emptyList())
        every { mockDashboardViewModel.lowStockItems } returns MutableStateFlow(emptyList())
        every { mockDashboardViewModel.salesList } returns MutableStateFlow(emptyList())
        every { mockDashboardViewModel.expensesList } returns MutableStateFlow(emptyList())
        every { mockDashboardViewModel.uiSyncStatus } returns
            MutableStateFlow(
                UiSyncStatus(
                    status = "DONE",
                    lastSyncAt = System.currentTimeMillis(),
                    failedCount = 0,
                    isOnline = true,
                ),
            )
        every { mockDashboardViewModel.purchases } returns MutableStateFlow(emptyList())
        every { mockDashboardViewModel.todaySnapshot } returns MutableStateFlow(DashboardViewModel.TodaySnapshot())
        every { mockDashboardViewModel.last7DaysData } returns
            MutableStateFlow(Triple(emptyList<Double>(), emptyList<Double>(), emptyList<Double>()))

        // Setup base state for SalesViewModel
        every { mockSalesViewModel.lastSaleId } returns null
        every { mockSalesViewModel.lastSaleTime } returns 0L
        every { mockSalesViewModel.cartItems } returns mutableListOf()
    }

    @Test
    @Config(qualifiers = RobolectricDeviceQualifiers.Pixel5)
    fun captureDashboardScreen_default_light() {
        composeTestRule.setContent {
            CompositionLocalProvider(LocalAppTheme provides themeManager) {
                StoreBookTheme(darkTheme = false) {
                    DashboardScreen(
                        navController = navController,
                        viewModel = mockDashboardViewModel,
                        salesViewModel = mockSalesViewModel,
                    )
                }
            }
        }
        composeTestRule.onRoot().captureRoboImage("src/test/snapshots/DashboardScreen_Phone_Light.png")
    }

    @Test
    @Config(qualifiers = RobolectricDeviceQualifiers.Pixel5)
    fun captureDashboardScreen_default_dark() {
        composeTestRule.setContent {
            CompositionLocalProvider(LocalAppTheme provides themeManager) {
                StoreBookTheme(darkTheme = true) {
                    DashboardScreen(
                        navController = navController,
                        viewModel = mockDashboardViewModel,
                        salesViewModel = mockSalesViewModel,
                    )
                }
            }
        }
        composeTestRule.onRoot().captureRoboImage("src/test/snapshots/DashboardScreen_Phone_Dark.png")
    }

    @Test
    @Config(qualifiers = "w1280dp-h800dp")
    fun captureDashboardScreen_tablet_layout() {
        composeTestRule.setContent {
            CompositionLocalProvider(LocalAppTheme provides themeManager) {
                StoreBookTheme(darkTheme = false) {
                    DashboardScreen(
                        navController = navController,
                        viewModel = mockDashboardViewModel,
                        salesViewModel = mockSalesViewModel,
                    )
                }
            }
        }
        composeTestRule.onRoot().captureRoboImage("src/test/snapshots/DashboardScreen_Tablet.png")
    }

    @Test
    @Config(qualifiers = RobolectricDeviceQualifiers.Pixel5)
    fun captureDashboardScreen_fontScale200() {
        composeTestRule.setContent {
            val customDensity = Density(fontScale = 2.0f, density = LocalDensity.current.density)
            CompositionLocalProvider(LocalDensity provides customDensity, LocalAppTheme provides themeManager) {
                StoreBookTheme(darkTheme = false) {
                    DashboardScreen(
                        navController = navController,
                        viewModel = mockDashboardViewModel,
                        salesViewModel = mockSalesViewModel,
                    )
                }
            }
        }
        composeTestRule.onRoot().captureRoboImage("src/test/snapshots/DashboardScreen_FontScale200.png")
    }

    @Test
    @Config(qualifiers = RobolectricDeviceQualifiers.Pixel5)
    fun captureDashboardScreen_longText_autoMarquee() {
        every { mockDashboardViewModel.uiSyncStatus } returns
            MutableStateFlow(
                UiSyncStatus(status = "FAILED", lastSyncAt = 0L, failedCount = 5, isOnline = false),
            )
        // Extremely long numbers to test sparkline card text wrapping
        every { mockDashboardViewModel.todaySnapshot } returns
            MutableStateFlow(
                DashboardViewModel.TodaySnapshot(
                    todayRevenue = 99999999999999.0,
                    todayExpenses = 99999999999999.0,
                    todayProfit = 99999999999999.0,
                ),
            )

        composeTestRule.setContent {
            CompositionLocalProvider(LocalAppTheme provides themeManager) {
                StoreBookTheme(darkTheme = false) {
                    DashboardScreen(
                        navController = navController,
                        viewModel = mockDashboardViewModel,
                        salesViewModel = mockSalesViewModel,
                    )
                }
            }
        }
        composeTestRule.onRoot().captureRoboImage("src/test/snapshots/DashboardScreen_LongText.png")
    }
}
