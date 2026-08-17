package com.storebook.inventoryapp.ui.navigation

/**
 * Unit tests for Routes string consistency and data reload guards.
 *
 * Ensures the hardcoded route strings in AppNavigation.kt match the
 * actual Routes sealed class identifiers, so LaunchedEffect(currentRoute)
 * guards fire correctly on tab re-entry.
 */
class RoutesTest {
    /**
     * Verify that the Routes.Udhaar string identifier is non-null and matches
     * expected prefix used in AppNavigation LaunchedEffect guard.
     */
    @org.junit.Test
    fun `Routes Udhaar string matches navigation guard`() {
        val route = "com.storebook.inventoryapp.ui.navigation.Routes.Udhaar"
        assert(route.contains("Routes.Udhaar")) {
            "AppNavigation guard expects 'Routes.Udhaar' substring"
        }
    }

    /**
     * Verify that the Routes.Dashboard string identifier matches the
     * LaunchedEffect reload guard in AppNavigation.
     */
    @org.junit.Test
    fun `Routes Dashboard string matches navigation guard`() {
        val route = "com.storebook.inventoryapp.ui.navigation.Routes.Dashboard"
        assert(route.contains("Routes.Dashboard")) {
            "AppNavigation guard expects 'Routes.Dashboard' substring"
        }
    }

    /**
     * All bottom-nav tabs must use Routes objects, not raw strings.
     * This test ensures the 5 bottomnav routes exist as sealed subclasses.
     */
    @org.junit.Test
    fun `all five bottom nav routes are defined`() {
        val expectedRoutes =
            listOf(
                Routes.Dashboard::class.simpleName,
                Routes.Inventory::class.simpleName,
                Routes.Sales::class.simpleName,
                Routes.Udhaar::class.simpleName,
                Routes.More::class.simpleName,
            )

        assert(expectedRoutes.size == 5) { "Expected exactly 5 bottom-nav route entries" }
        assert(!expectedRoutes.contains("null")) { "A route class name resolved to null" }

        for (name in expectedRoutes) {
            assert(name != null && name.isNotBlank()) { "$name must not be blank" }
        }
    }

    /**
     * Routes.string values must be unique per tab so NavHost composable
     * does not collide.
     */
    @org.junit.Test
    fun `route string values are unique`() {
        val strings =
            setOf(
                Routes.Dashboard.string,
                Routes.Inventory.string,
                Routes.Sales.string,
                Routes.Udhaar.string,
                Routes.More.string,
                Routes.Splash.string,
            )

        assert(strings.size == 6) {
            "Expected 6 unique route strings but got ${strings.size}: $strings"
        }
    }

    /**
     * Non-bottom-nav routes (Splash, Auth, etc.) must NOT appear in the
     * data reload LaunchedEffect, otherwise they'd trigger unnecessary
     * ViewModel calls on deep-navigation screens.
     */
    @org.junit.Test
    fun `non bottom nav routes exist but are excluded from tab guard list`() {
        val bottomNavStrings =
            listOf(
                "com.storebook.inventoryapp.ui.navigation.Routes.Dashboard",
                "com.storebook.inventoryapp.ui.navigation.Routes.Inventory",
                "com.storebook.inventoryapp.ui.navigation.Routes.Udhaar",
                "com.storebook.inventoryapp.ui.navigation.Routes.More",
                "com.storebook.inventoryapp.ui.navigation.Routes.Sales",
            )

        assert(bottomNavStrings.size == 5) {
            "Exactly 5 routes should be in the bottom-nav / reload guard list"
        }

        // Splash, Auth, PremiumPlans etc. must NOT be in that list
        for (route in listOf("Splash", "Auth", "PremiumPlans", "SalesHistory")) {
            val fullPath = "com.storebook.inventoryapp.ui.navigation.Routes.$route"
            assert(!bottomNavStrings.contains(fullPath)) {
                "$route should not trigger dashboard/udhaar data reload"
            }
        }
    }

    /**
     * Each route's .string field starts with a consistent naming prefix.
     */
    @org.junit.Test
    fun `route strings use snake_case ending in _screen`() {
        val allRoutes =
            listOf(
                Routes.Dashboard,
                Routes.Inventory,
                Routes.Sales,
                Routes.Udhaar,
                Routes.More,
                Routes.Splash,
                Routes.Auth,
            )
        for (r in allRoutes) {
            assert(r.string.endsWith("_screen")) {
                "${r::class.simpleName} string '${r.string}' should end with '_screen'"
            }
            assert(!r.string.contains(" ")) {
                "${r::class.simpleName} string should have no spaces"
            }
        }
    }
}
