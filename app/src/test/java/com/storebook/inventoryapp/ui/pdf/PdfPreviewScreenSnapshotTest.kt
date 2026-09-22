package com.storebook.inventoryapp.ui.pdf

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.core.app.ApplicationProvider
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import com.storebook.inventoryapp.shared.domain.models.CartItem
import com.storebook.inventoryapp.shared.domain.models.Item
import com.storebook.inventoryapp.ui.pdf.EstimatePdfPreviewScreen
import com.storebook.inventoryapp.ui.pdf.InvoicePdfPreviewScreen
import com.storebook.inventoryapp.ui.theme.LocalAppTheme
import com.storebook.inventoryapp.ui.theme.ManualThemeManager
import com.storebook.inventoryapp.ui.theme.StoreBookTheme
import io.mockk.mockk
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
class PdfPreviewScreenSnapshotTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var themeManager: ManualThemeManager

    private val cartItems: List<CartItem> =
        listOf(
            CartItem(
                Item(
                    name = "Milk",
                    quantity = 10.0,
                    unit = "litre",
                    buyPrice = 30.0,
                    sellPrice = 45.0,
                    lowStockThreshold = 5.0,
                    category = "Dairy",
                ),
                2.0,
            ),
            CartItem(
                Item(
                    name = "Bread",
                    quantity = 20.0,
                    unit = "piece",
                    buyPrice = 20.0,
                    sellPrice = 35.0,
                    lowStockThreshold = 5.0,
                    category = "Bakery",
                ),
                3.0,
            ),
        )

    @Before
    fun setup() {
        themeManager = ManualThemeManager(ApplicationProvider.getApplicationContext())
    }

    @Test
    @Config(qualifiers = RobolectricDeviceQualifiers.Pixel5)
    fun captureInvoicePreview_light() {
        composeTestRule.setContent {
            CompositionLocalProvider(LocalAppTheme provides themeManager) {
                StoreBookTheme(darkTheme = false) {
                    InvoicePdfPreviewScreen(
                        saleId = 1001L,
                        cartItems = cartItems,
                        totalAmount = 195.0,
                        onBack = mockk<() -> Unit>(relaxed = true)::invoke,
                    )
                }
            }
        }
        composeTestRule.onRoot().captureRoboImage(
            "src/test/snapshots/InvoicePdfPreviewScreen_Phone_Light.png",
        )
    }

    @Test
    @Config(qualifiers = RobolectricDeviceQualifiers.Pixel5)
    fun captureInvoicePreview_dark() {
        composeTestRule.setContent {
            CompositionLocalProvider(LocalAppTheme provides themeManager) {
                StoreBookTheme(darkTheme = true) {
                    InvoicePdfPreviewScreen(
                        saleId = 1001L,
                        cartItems = cartItems,
                        totalAmount = 195.0,
                        onBack = mockk<() -> Unit>(relaxed = true)::invoke,
                    )
                }
            }
        }
        composeTestRule.onRoot().captureRoboImage(
            "src/test/snapshots/InvoicePdfPreviewScreen_Phone_Dark.png",
        )
    }

    @Test
    @Config(qualifiers = RobolectricDeviceQualifiers.Pixel5)
    fun captureEstimatePreview_light() {
        composeTestRule.setContent {
            CompositionLocalProvider(LocalAppTheme provides themeManager) {
                StoreBookTheme(darkTheme = false) {
                    EstimatePdfPreviewScreen(
                        estimateId = 2001L,
                        cartItems = cartItems,
                        totalAmount = 195.0,
                        onBack = mockk<() -> Unit>(relaxed = true)::invoke,
                    )
                }
            }
        }
        composeTestRule.onRoot().captureRoboImage(
            "src/test/snapshots/EstimatePdfPreviewScreen_Phone_Light.png",
        )
    }

    @Test
    @Config(qualifiers = RobolectricDeviceQualifiers.Pixel5)
    fun captureEstimatePreview_dark() {
        composeTestRule.setContent {
            CompositionLocalProvider(LocalAppTheme provides themeManager) {
                StoreBookTheme(darkTheme = true) {
                    EstimatePdfPreviewScreen(
                        estimateId = 2001L,
                        cartItems = cartItems,
                        totalAmount = 195.0,
                        onBack = mockk<() -> Unit>(relaxed = true)::invoke,
                    )
                }
            }
        }
        composeTestRule.onRoot().captureRoboImage(
            "src/test/snapshots/EstimatePdfPreviewScreen_Phone_Dark.png",
        )
    }
}
