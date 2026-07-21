package com.storebook.inventoryapp.shared.test

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll

/**
 * e31-s1: Global Main dispatcher override so all `withContext(Dispatchers.Main)` blocks
 * in ViewModels / repos execute deterministically during JVM tests.
 *
 * Usage: Annotate test classes with `@TestInstance(TestInstance.Lifecycle.PER_CLASS)` and this rule runs once.
 */
object TestDispatcherRule {
    val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()

    @BeforeAll
    @Suppress("unused")
    fun setMainDispatcher() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterAll
    @Suppress("unused")
    fun resetMainDispatcher() {
        Dispatchers.resetMain()
    }
}
