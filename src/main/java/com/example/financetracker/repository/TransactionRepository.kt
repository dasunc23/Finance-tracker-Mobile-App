package com.example.financetracker.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.financetracker.model.Budget
import com.example.financetracker.model.Category
import com.example.financetracker.model.Transaction
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Calendar
import java.util.Date
import java.util.UUID

/**
 * Repository for handling transaction and budget data
 */
class TransactionRepository(private val context: Context) {
    
    companion object {
        private const val PREF_NAME = "finance_tracker_data"
        private const val KEY_TRANSACTIONS = "transactions"
        private const val KEY_BUDGETS = "budgets"
    }
    
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()
    
    /**
     * Get all transactions
     */
    fun getAllTransactions(): List<Transaction> {
        val transactionsJson = sharedPreferences.getString(KEY_TRANSACTIONS, null) ?: return emptyList()
        val type = object : TypeToken<List<Transaction>>() {}.type
        return gson.fromJson(transactionsJson, type) ?: emptyList()
    }
    
    /**
     * Add a new transaction
     */
    fun addTransaction(transaction: Transaction) {
        val transactions = getAllTransactions().toMutableList()
        transactions.add(transaction)
        saveTransactions(transactions)
    }
    
    /**
     * Update an existing transaction
     */
    fun updateTransaction(transaction: Transaction) {
        val transactions = getAllTransactions().toMutableList()
        val index = transactions.indexOfFirst { it.id == transaction.id }
        if (index != -1) {
            transactions[index] = transaction
            saveTransactions(transactions)
        }
    }
    
    /**
     * Delete a transaction
     */
    fun deleteTransaction(transactionId: String) {
        val transactions = getAllTransactions().toMutableList()
        transactions.removeAll { it.id == transactionId }
        saveTransactions(transactions)
    }
    
    /**
     * Get transactions for a specific month and year
     */
    fun getTransactionsForMonth(month: Int, year: Int): List<Transaction> {
        val transactions = getAllTransactions()
        val calendar = Calendar.getInstance()
        
        return transactions.filter { transaction ->
            calendar.time = transaction.date
            calendar.get(Calendar.MONTH) == month && calendar.get(Calendar.YEAR) == year
        }
    }
    
    /**
     * Get transactions by category
     */
    fun getTransactionsByCategory(category: Category): List<Transaction> {
        return getAllTransactions().filter { it.category == category }
    }
    
    /**
     * Get total income for a specific month and year
     */
    fun getTotalIncomeForMonth(month: Int, year: Int): Double {
        return getTransactionsForMonth(month, year)
            .filter { !it.isExpense }
            .sumOf { it.amount }
    }
    
    /**
     * Get total expenses for a specific month and year
     */
    fun getTotalExpensesForMonth(month: Int, year: Int): Double {
        return getTransactionsForMonth(month, year)
            .filter { it.isExpense }
            .sumOf { it.amount }
    }
    
    /**
     * Get expenses by category for a specific month and year
     */
    fun getExpensesByCategory(month: Int, year: Int): Map<Category, Double> {
        val expensesByCategory = mutableMapOf<Category, Double>()
        
        getTransactionsForMonth(month, year)
            .filter { it.isExpense }
            .forEach { transaction ->
                val currentAmount = expensesByCategory[transaction.category] ?: 0.0
                expensesByCategory[transaction.category] = currentAmount + transaction.amount
            }
        
        return expensesByCategory
    }
    
    /**
     * Save list of transactions to SharedPreferences
     */
    private fun saveTransactions(transactions: List<Transaction>) {
        val transactionsJson = gson.toJson(transactions)
        sharedPreferences.edit().putString(KEY_TRANSACTIONS, transactionsJson).apply()
    }
    
    /**
     * Get all budgets
     */
    fun getAllBudgets(): List<Budget> {
        val budgetsJson = sharedPreferences.getString(KEY_BUDGETS, null) ?: return emptyList()
        val type = object : TypeToken<List<Budget>>() {}.type
        return gson.fromJson(budgetsJson, type) ?: emptyList()
    }
    
    /**
     * Get budget for a specific month and year
     */
    fun getBudgetForMonth(month: Int, year: Int): Budget? {
        return getAllBudgets().find { it.month == month && it.year == year }
    }
    
    /**
     * Get budget for a specific category, month, and year
     */
    fun getBudgetForCategory(category: Category, month: Int, year: Int): Budget? {
        return getAllBudgets().find { 
            it.category == category && it.month == month && it.year == year 
        }
    }
    
    /**
     * Add or update a budget
     */
    fun saveBudget(budget: Budget) {
        val budgets = getAllBudgets().toMutableList()
        val index = budgets.indexOfFirst { 
            (it.category == budget.category) && (it.month == budget.month) && (it.year == budget.year) 
        }
        
        if (index != -1) {
            budgets[index] = budget
        } else {
            budgets.add(budget)
        }
        
        saveBudgets(budgets)
    }
    
    /**
     * Delete a budget
     */
    fun deleteBudget(budgetId: String) {
        val budgets = getAllBudgets().toMutableList()
        budgets.removeAll { it.id == budgetId }
        saveBudgets(budgets)
    }
    
    /**
     * Save list of budgets to SharedPreferences
     */
    private fun saveBudgets(budgets: List<Budget>) {
        val budgetsJson = gson.toJson(budgets)
        sharedPreferences.edit().putString(KEY_BUDGETS, budgetsJson).apply()
    }
    
    /**
     * Generate sample data for demo purposes
     */
    fun generateSampleData() {
        // Clear existing data
        sharedPreferences.edit().clear().apply()
        
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)
        
        // Create a budget
        val budget = Budget(
            id = UUID.randomUUID().toString(),
            amount = 1500.0,
            month = currentMonth,
            year = currentYear
        )
        saveBudget(budget)
        
        // Create sample transactions
        val transactions = mutableListOf<Transaction>()
        
        // Set calendar to start of month
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        
        // Sample expense transactions
        transactions.add(
            Transaction(
                title = "Grocery Shopping",
                amount = 75.42,
                date = calendar.time,
                category = Category.FOOD,
                isExpense = true
            )
        )
        
        calendar.add(Calendar.DAY_OF_MONTH, 2)
        transactions.add(
            Transaction(
                title = "Gas Station",
                amount = 42.50,
                date = calendar.time,
                category = Category.TRANSPORT,
                isExpense = true
            )
        )
        
        calendar.add(Calendar.DAY_OF_MONTH, 3)
        transactions.add(
            Transaction(
                title = "Movie Tickets",
                amount = 25.00,
                date = calendar.time,
                category = Category.ENTERTAINMENT,
                isExpense = true
            )
        )
        
        calendar.add(Calendar.DAY_OF_MONTH, 5)
        transactions.add(
            Transaction(
                title = "Pharmacy",
                amount = 32.80,
                date = calendar.time,
                category = Category.HEALTH,
                isExpense = true
            )
        )
        
        // Sample income transaction
        calendar.set(Calendar.DAY_OF_MONTH, 15)
        transactions.add(
            Transaction(
                title = "Monthly Salary",
                amount = 3000.00,
                date = calendar.time,
                category = Category.SALARY,
                isExpense = false
            )
        )
        
        // Add more sample transactions for the last two weeks
        calendar.set(Calendar.DAY_OF_MONTH, 16)
        transactions.add(
            Transaction(
                title = "Internet Bill",
                amount = 65.99,
                date = calendar.time,
                category = Category.UTILITIES,
                isExpense = true
            )
        )
        
        calendar.add(Calendar.DAY_OF_MONTH, 2)
        transactions.add(
            Transaction(
                title = "Dinner Out",
                amount = 48.75,
                date = calendar.time,
                category = Category.FOOD,
                isExpense = true
            )
        )
        
        calendar.add(Calendar.DAY_OF_MONTH, 3)
        transactions.add(
            Transaction(
                title = "New Shoes",
                amount = 89.99,
                date = calendar.time,
                category = Category.SHOPPING,
                isExpense = true
            )
        )
        
        // Save transactions
        saveTransactions(transactions)
    }
} 