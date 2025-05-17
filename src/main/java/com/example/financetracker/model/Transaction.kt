package com.example.financetracker.model

import java.util.Date
import java.util.UUID

/**
 * Data class representing a financial transaction
 */
data class Transaction(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val amount: Double,
    val date: Date,
    val category: Category,
    val isExpense: Boolean = true,
    val note: String = ""
) {
    /**
     * Get sign-prefixed amount based on transaction type (expense/income)
     */
    fun getSignedAmount(): Double {
        return if (isExpense) -amount else amount
    }

    /**
     * Get formatted amount with currency symbol
     */
    fun getFormattedAmount(): String {
        val prefix = if (isExpense) "-Rs " else "+Rs "
        return "$prefix${"%.2f".format(amount)}"
    }
} 