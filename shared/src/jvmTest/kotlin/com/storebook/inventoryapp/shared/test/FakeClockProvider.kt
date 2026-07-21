package com.storebook.inventoryapp.shared.test

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

/**
 * e31-s1: Deterministic fake Clock for timestamp-based logic.
 *
 * All tests that create repositories/ViewModels should pass this so SQL `updated_at`,
 * `deleted_timestamp`, sync timestamps, date-range queries etc. are reproducible every run.
 */
class FakeClock(
    var frozenInstant: Instant = Instant.parse("2026-07-15T04:30:00Z"), // IST-approx midnight
) : Clock {
    override fun now(): Instant = frozenInstant

    fun tick(seconds: Long) {
        frozenInstant += kotlin.time.Duration.parse("${seconds}s")
    }

    companion object {
        @Volatile
        private var _instance: FakeClock? = null

        /** Singleton so all tests share the same frozen reference. */
        var instance: FakeClock
            get() = _instance ?: FakeClock().also { _instance = it }
            set(value) { _instance = value }

        fun reset() {
            _instance = FakeClock(Instant.parse("2026-07-15T04:30:00Z"))
        }
    }
}
