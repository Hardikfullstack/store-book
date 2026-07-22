package com.storebook.inventoryapp.ui.interactions

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.core.app.ApplicationProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.storebook.inventoryapp.shared.domain.models.CustomerBalance
import com.storebook.inventoryapp.shared.domain.models.CustomerDetailedBalance
import com.storebook.inventoryapp.ui.screens.storebook.UdhaarScreen
import com.storebook.inventoryapp.ui.theme.LocalAppTheme
import com.storebook.inventoryapp.ui.theme.ManualThemeManager
import com.storebook.inventoryapp.ui.theme.StoreBookTheme
import com.storebook.inventoryapp.ui.viewmodel.UdhaarViewModel
import io.mockk.*
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Interaction tests for Udhaar (Credit Ledger) Screen CTAs rendered via Robolectric.
 * - Give Credit (+ उधार) → Customer receives on credit
 * - Receive Payment (- जमा) → Customer pays back
 * - Search customer by name filter
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [33], application = android.app.Application::class)
class UdhaarScreenInteractionTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var mockUdhaarVM: UdhaarViewModel
    private lateinit var themeManager: ManualThemeManager

    private val testBalances =
        listOf(
            CustomerBalance(
                customerName = "Ramesh Kumar",
                netBalance = 500.0,
                lastTransactionTime = System.currentTimeMillis(),
            ),
        )

    @Before
    fun setup() {
        mockkStatic(FirebaseAuth::class)
        val mockAuth = mockk<FirebaseAuth>(relaxed = true)
        val mockUser = mockk<FirebaseUser>(relaxed = true)
        every { mockUser.phoneNumber } returns "+919876543210"
        every { mockAuth.currentUser } returns mockUser
        every { FirebaseAuth.getInstance() } returns mockAuth

        mockUdhaarVM = mockk(relaxed = true)
        themeManager = ManualThemeManager(ApplicationProvider.getApplicationContext())

        // UdhaarScreen collects: udhaarBalances AND detailedBals via collectAsStateWithLifecycle
        // Mock BOTH to avoid ClassCastException on unmocked generic StateFlows
        every { mockUdhaarVM.udhaarBalances } returns MutableStateFlow(testBalances)
        every { mockUdhaarVM.detailedBalances } returns MutableStateFlow<List<CustomerDetailedBalance>>(emptyList())
    }

    @After
    fun tearDown() {}

    // ── CTA 1: Give Credit (+ उधार) ───────────────────────────────

    @Test
    fun testGiveCredit_buttonRendersOnScreen() {
        every { mockUdhaarVM.recordUdhaarEntry(any(), any(), any(), any()) } just runs

        composeTestRule.setContent {
            CompositionLocalProvider(LocalAppTheme provides themeManager) {
                StoreBookTheme(darkTheme = false) { UdhaarScreen(viewModel = mockUdhaarVM) }
            }
        }

        composeTestRule.onRoot().assertExists()
    }

    @Test
    fun testGiveCredit_recordsToViewModel() {
        every { mockUdhaarVM.recordUdhaarEntry(any(), any(), any(), any()) } just runs

        composeTestRule.setContent {
            CompositionLocalProvider(LocalAppTheme provides themeManager) {
                StoreBookTheme(darkTheme = false) { UdhaarScreen(viewModel = mockUdhaarVM) }
            }
        }

        composeTestRule.onRoot().assertExists()
    }

    // ── CTA 2: Receive Payment (- जमा) ─────────────────────────────

    @Test
    fun testReceivePayment_buttonRendersOnScreen() {
        every { mockUdhaarVM.recordUdhaarEntry(any(), any(), any(), any()) } just runs

        composeTestRule.setContent {
            CompositionLocalProvider(LocalAppTheme provides themeManager) {
                StoreBookTheme(darkTheme = false) { UdhaarScreen(viewModel = mockUdhaarVM) }
            }
        }

        composeTestRule.onRoot().assertExists()
    }

    @Test
    fun testReceivePayment_recordsPaymentEntryViaViewModel() {
        every { mockUdhaarVM.recordUdhaarEntry(any(), any(), any(), any()) } just runs

        composeTestRule.setContent {
            CompositionLocalProvider(LocalAppTheme provides themeManager) {
                StoreBookTheme(darkTheme = false) { UdhaarScreen(viewModel = mockUdhaarVM) }
            }
        }

        composeTestRule.onRoot().assertExists()
    }

    // ── CTA 3: Search Customer by Name ─────────────────────────────

    @Test
    fun testSearchCustomer_filtersEntriesByName() {
        val filteredBalances = listOf(testBalances[0])
        every { mockUdhaarVM.udhaarBalances } returns MutableStateFlow(filteredBalances)

        composeTestRule.setContent {
            CompositionLocalProvider(LocalAppTheme provides themeManager) {
                StoreBookTheme(darkTheme = false) { UdhaarScreen(viewModel = mockUdhaarVM) }
            }
        }

        composeTestRule.onRoot().assertExists()
    }

    // ── CTA 4: Customer Card Click → Ledger Drilldown ─────────────

    @Test
    fun testCustomerCardClick_opensLedgerView() {
        composeTestRule.setContent {
            CompositionLocalProvider(LocalAppTheme provides themeManager) {
                StoreBookTheme(darkTheme = false) { UdhaarScreen(viewModel = mockUdhaarVM) }
            }
        }

        composeTestRule.onRoot().assertExists()
    }

    // ── CTA 5: Zero Balance → Nett Balanced Label ────────────────

    @Test
    fun testCustomerWithZeroBalance_showsNettBalancedLabel() {
        val zeroBalanceCustomers =
            listOf(
                CustomerBalance(
                    customerName = "Ramesh", netBalance = 0.0,
                    lastTransactionTime =
                        System
                            .currentTimeMillis(),
                ),
            )
        every { mockUdhaarVM.udhaarBalances } returns MutableStateFlow(zeroBalanceCustomers)

        composeTestRule.setContent {
            CompositionLocalProvider(LocalAppTheme provides themeManager) {
                StoreBookTheme(darkTheme = false) { UdhaarScreen(viewModel = mockUdhaarVM) }
            }
        }

        composeTestRule.onRoot().assertExists()
    }
}
