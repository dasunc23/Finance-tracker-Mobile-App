package com.example.financetracker.model

import java.util.UUID

/**
 * Data class representing a budget
 */
data class Budget(
    val id: String = UUID.randomUUID().toString(),
    val amount: Double,
    val category: Category? = null,
    val month: Int,
    val year: Int,
    val alertThreshold: Int = 80 // Percentage threshold for alerts (default 80%)
) {
    /**
     * Check if the amount spent exceeds the alert threshold
     *
     * @param amountSpent The current amount spent
     * @return True if the threshold is exceeded
     */
    fun isThresholdExceeded(amountSpent: Double): Boolean {
        val percentageSpent = (amountSpent / amount) * 100
        return percentageSpent >= alertThreshold
    }

    /**
     * Check if the budget is exceeded
     *
     * @param amountSpent The current amount spent
     * @return True if the budget is exceeded
     */
    fun isBudgetExceeded(amountSpent: Double): Boolean {
        return amountSpent >= amount
    }

    /**
     * Calculate the remaining budget
     *
     * @param amountSpent The current amount spent
     * @return The remaining budget
     */
    fun calculateRemainingBudget(amountSpent: Double): Double {
        return amount - amountSpent
    }

    /**
     * Calculate the percentage spent
     *
     * @param amountSpent The current amount spent
     * @return The percentage spent
     */
    fun calculatePercentageSpent(amountSpent: Double): Int {
        return ((amountSpent / amount) * 100).toInt().coerceIn(0, 100)
    }
} 