package com.storebook.inventoryapp.shared.util

import kotlin.math.pow

/**
 * E01-S2: Exponential backoff calculator for sync retry delays.
 * Delays: attempt 0 → 5s, attempt 1 → 10s, attempt 2 → 20s (capped at maxDelayMs)
 */
object RetryBackoffCalculator {

    const val DEFAULT_MAX_RETRIES = 3
    const val BASE_DELAY_MS = 5_000L      // 5 seconds
    const val MAX_DELAY_MS = 300_000L     // 5 minutes cap

    /**
     * Calculate delay for next retry attempt using exponential backoff.
     * Formula: min(BASE_DELAY × 2^retryCount, MAX_DELAY)
     */
    fun nextDelayMillis(retryCount: Int): Long {
        require(retryCount >= 0) { "retryCount must be non-negative" }
        val exponential = BASE_DELAY_MS * (2.0.pow(retryCount).toLong())
        return minOf(exponential, MAX_DELAY_MS)
    }

    /** Schedule absolute retry timestamp from now */
    fun nextRetryAtFromNow(retryCount: Int): Long {
        return System.currentTimeMillis() + nextDelayMillis(retryCount)
    }

    /** Check if item has exhausted all retries */
    fun hasMaxRetries(retryCount: Int, maxRetries: Int = DEFAULT_MAX_RETRIES): Boolean =
        retryCount >= maxRetries

    // === Quick reference table ===
    // attempt 0: 5_000ms   (5s)
    // attempt 1: 10_000ms  (10s)
    // attempt 2: 20_000ms  (20s)
    // attempt 3+: capped at 300_000ms (5min)
}
