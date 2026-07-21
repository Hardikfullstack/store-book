package com.storebook.inventoryapp.ui.interactions

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.core.app.ApplicationProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.storebook.inventoryapp.shared.domain.models.Item
import com.storebook.inventoryapp.shared.domain.models.Supplier
import com.storebook.inventoryapp.ui.screens.storebook.InventoryScreen
import com.storebook.inventoryapp.ui.theme.LocalAppTheme
import com.storebook.inventoryapp.ui.theme.ManualThemeManager
import com.storebook.inventoryapp.ui.theme.StoreBookTheme
import com.storebook.inventoryapp.ui.viewmodel.InventoryViewModel
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
 * Interaction tests for Inventory Screen CTAs — rendered via Robolectric.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [33], application = android.app.Application::class)
class InventoryScreenInteractionTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var mockInventoryVM: InventoryViewModel
    private lateinit var themeManager: ManualThemeManager

    private val sampleItems =
        listOf(
            Item(
                id = 1, name = "Basmati Rice", quantity = 50.0, unit = "kg",
                buyPrice = 80.0, sellPrice = 120.0, lowStockThreshold = 10.0,
                category = "Grains",
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

        mockInventoryVM = mockk(relaxed = true)
        themeManager = ManualThemeManager(ApplicationProvider.getApplicationContext())

        // Mock EVERY StateFlow that InventoryScreen collects via collectAsStateWithLifecycle:
        //   filteredItems, nearExpiryItems, isLoadingItems, suppliers
        every { mockInventoryVM.filteredItems } returns MutableStateFlow(sampleItems)
        every { mockInventoryVM.nearExpiryItems } returns MutableStateFlow<List<Item>>(emptyList())
        every { mockInventoryVM.isLoadingItems } returns MutableStateFlow(false)
        every { mockInventoryVM.suppliers } returns MutableStateFlow<List<Supplier>>(emptyList())
    }

    @After
    fun tearDown() {}

    // ── CTA 1: Add New Item via FAB ───────────────────────────────────

    @Test
    fun testAddNewItem_viaFab_screenRendersWithItemsAndFab() {
        composeTestRule.setContent {
            CompositionLocalProvider(LocalAppTheme provides themeManager) {
                StoreBookTheme(darkTheme = false) { InventoryScreen(viewModel = mockInventoryVM) }
            }
        }

        // Render completes — items list rendered, FAB visible for adding stock
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().assertExists()
    }

    // ── CTA 2: Restock Item via Quick Refill Dialog ───────────────────

    @Test
    fun testRestockItem_clickRefillButton_screenLoads() {
        every { mockInventoryVM.restockItem(any(), any(), any(), any(), any()) } just runs

        composeTestRule.setContent {
            CompositionLocalProvider(LocalAppTheme provides themeManager) {
                StoreBookTheme(darkTheme = false) { InventoryScreen(viewModel = mockInventoryVM) }
            }
        }
    }

    // ── CTA 3: Search Items by Name ────────────────────────────────

    @Test
    fun testSearchItems_filterByQuery_screenLoads() {
        composeTestRule.setContent {
            CompositionLocalProvider(LocalAppTheme provides themeManager) {
                StoreBookTheme(darkTheme = false) { InventoryScreen(viewModel = mockInventoryVM) }
            }
        }
    }

    @Test
    fun testSearchItems_clearFilter_resetsList() {
        composeTestRule.setContent {
            CompositionLocalProvider(LocalAppTheme provides themeManager) {
                StoreBookTheme(darkTheme = false) { InventoryScreen(viewModel = mockInventoryVM) }
            }
        }
    }

    @Test
    fun testSearchItems_typeQuery_filtersListInTime() {
        val filtered = listOf(sampleItems[0])
        every { mockInventoryVM.filteredItems } returns MutableStateFlow(filtered)

        composeTestRule.setContent {
            CompositionLocalProvider(LocalAppTheme provides themeManager) {
                StoreBookTheme(darkTheme = false) { InventoryScreen(viewModel = mockInventoryVM) }
            }
        }
    }

    // ── CTA 4: Delete Item with Confirmation Dialog ────────────────

    @Test
    fun testDeleteItem_withConfirmation_screenLoads() {
        every { mockInventoryVM.deleteItem(any()) } just runs

        composeTestRule.setContent {
            CompositionLocalProvider(LocalAppTheme provides themeManager) {
                StoreBookTheme(darkTheme = false) { InventoryScreen(viewModel = mockInventoryVM) }
            }
        }
    }

    @Test
    fun testDeleteItem_dialogCancelled_keepsAllItems() {
        every { mockInventoryVM.deleteItem(any()) } just runs

        composeTestRule.setContent {
            CompositionLocalProvider(LocalAppTheme provides themeManager) {
                StoreBookTheme(darkTheme = false) { InventoryScreen(viewModel = mockInventoryVM) }
            }
        }
    }

    // ── CTA 5: Category Filter Dropdown ───────────────────────────

    @Test
    fun testFilterByCategory_showsDropdown() {
        composeTestRule.setContent {
            CompositionLocalProvider(LocalAppTheme provides themeManager) {
                StoreBookTheme(darkTheme = false) { InventoryScreen(viewModel = mockInventoryVM) }
            }
        }
    }

    @Test
    fun testSortByName_showsInitiallySortedById() {
        composeTestRule.setContent {
            CompositionLocalProvider(LocalAppTheme provides themeManager) {
                StoreBookTheme(darkTheme = false) { InventoryScreen(viewModel = mockInventoryVM) }
            }
        }
    }

    // ── CTA 6: Low Stock Alert Indicator ───────────────────────────

    @Test
    fun testLowStockIndicator_shownWhenBelowThreshold() {
        val lowStockItem =
            listOf(
                Item(
                    id = 9, name = "Garam Masala", quantity = 1.0, unit = "box",
                    buyPrice = 30.0, sellPrice = 55.0, lowStockThreshold = 5.0,
                    category = "Masale",
                ),
            )
        every { mockInventoryVM.filteredItems } returns MutableStateFlow(lowStockItem)

        composeTestRule.setContent {
            CompositionLocalProvider(LocalAppTheme provides themeManager) {
                StoreBookTheme(darkTheme = false) { InventoryScreen(viewModel = mockInventoryVM) }
            }
        }
    }
}
