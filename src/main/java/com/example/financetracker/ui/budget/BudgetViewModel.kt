package com.example.financetracker.ui.budget

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import com.example.financetracker.model.Budget
import com.example.financetracker.model.Category
import com.example.financetracker.repository.TransactionRepository
import com.example.financetracker.ui.transactions.TransactionsViewModel
import com.example.financetracker.util.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

class BudgetViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TransactionRepository(application)
    private val notificationHelper = NotificationHelper(application)
    
    private val _budget = MutableLiveData<Budget>()
    val budget: LiveData<Budget> = _budget
    
    private val _totalSpent = MutableLiveData<Double>()
    val totalSpent: LiveData<Double> = _totalSpent
    
    private val _percentageSpent = MutableLiveData<Int>()
    val percentageSpent: LiveData<Int> = _percentageSpent
    
    private val _remainingBudget = MutableLiveData<Double>()
    val remainingBudget: LiveData<Double> = _remainingBudget
    
    private val _formattedMonthYear = MutableLiveData<String>()
    val formattedMonthYear: LiveData<String> = _formattedMonthYear
    
    private val _categoryBudgets = MutableLiveData<List<CategoryBudgetItem>>()
    val categoryBudgets: LiveData<List<CategoryBudgetItem>> = _categoryBudgets
    
    // Event for showing alert dialog
    private val _showBudgetAlertEvent = MutableLiveData<BudgetAlertInfo?>()
    val showBudgetAlertEvent: LiveData<BudgetAlertInfo?> = _showBudgetAlertEvent
    
    private val calendar = Calendar.getInstance()
    private val currentMonth = calendar.get(Calendar.MONTH)
    private val currentYear = calendar.get(Calendar.YEAR)
    
    init {
        loadBudget()
        updateMonthYearFormat()
    }
    
    // Method to setup observation of TransactionsViewModel
    fun observeTransactionUpdates(transactionsViewModel: TransactionsViewModel) {
        transactionsViewModel.budgetUpdateTrigger.observeForever(transactionUpdateObserver)
    }
    
    // Observer for transaction updates
    private val transactionUpdateObserver = Observer<Long> {
        loadBudget()
    }
    
    fun loadBudget() {
        viewModelScope.launch {
            val monthlyBudget = withContext(Dispatchers.IO) {
                repository.getBudgetForMonth(currentMonth, currentYear)
            }
            
            if (monthlyBudget != null) {
                _budget.value = monthlyBudget
            } else {
                // Create a default budget if none exists
                val defaultBudget = Budget(
                    amount = 1500.0,
                    month = currentMonth,
                    year = currentYear
                )
                _budget.value = defaultBudget
            }
            
            updateExpenses()
            loadCategoryBudgets()
        }
    }
    
    private fun updateExpenses() {
        viewModelScope.launch {
            val spent = withContext(Dispatchers.IO) {
                repository.getTotalExpensesForMonth(currentMonth, currentYear)
            }
            
            _totalSpent.value = spent
            
            val budgetAmount = _budget.value?.amount ?: 0.0
            val percentage = if (budgetAmount > 0) {
                ((spent / budgetAmount) * 100).toInt().coerceIn(0, 100)
            } else {
                0
            }
            
            _percentageSpent.value = percentage
            _remainingBudget.value = (budgetAmount - spent).coerceAtLeast(0.0)
            
            // Check if we need to show a budget warning notification
            checkBudgetThreshold(spent)
        }
    }
    
    private fun checkBudgetThreshold(spent: Double) {
        val budget = _budget.value ?: return
        
        if (budget.isThresholdExceeded(spent) || budget.isBudgetExceeded(spent)) {
            val percentage = if (budget.amount > 0) {
                ((spent / budget.amount) * 100).toInt().coerceIn(0, 100)
            } else {
                0
            }
            
            // Show notification
            notificationHelper.showBudgetWarningNotification(percentage)
            
            // Trigger alert dialog
            _showBudgetAlertEvent.value = BudgetAlertInfo(
                percentage = percentage,
                spent = spent,
                budget = budget.amount,
                isExceeded = budget.isBudgetExceeded(spent)
            )
        }
    }
    
    // Clear the alert event after handling
    fun budgetAlertShown() {
        _showBudgetAlertEvent.value = null
    }
    
    private fun updateMonthYearFormat() {
        val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.MONTH, currentMonth)
        calendar.set(Calendar.YEAR, currentYear)
        
        _formattedMonthYear.value = monthFormat.format(calendar.time)
    }
    
    fun saveBudget(amount: Double, alertThreshold: Int) {
        viewModelScope.launch {
            val updatedBudget = Budget(
                id = _budget.value?.id ?: UUID.randomUUID().toString(),
                amount = amount,
                month = currentMonth,
                year = currentYear,
                alertThreshold = alertThreshold
            )
            
            withContext(Dispatchers.IO) {
                repository.saveBudget(updatedBudget)
            }
            
            _budget.value = updatedBudget
            updateExpenses()
        }
    }
    
    private fun loadCategoryBudgets() {
        viewModelScope.launch {
            val expensesByCategory = withContext(Dispatchers.IO) {
                repository.getExpensesByCategory(currentMonth, currentYear)
            }
            
            val categoryBudgetItems = mutableListOf<CategoryBudgetItem>()
            
            // Add items for categories with expenses
            for ((category, amount) in expensesByCategory) {
                // Check if there's a specific budget for this category
                val categoryBudget = withContext(Dispatchers.IO) {
                    repository.getBudgetForCategory(category, currentMonth, currentYear)
                }
                
                val budgetLimit = categoryBudget?.amount ?: (_budget.value?.amount ?: 0.0) / 4
                val percentage = ((amount / budgetLimit) * 100).toInt().coerceIn(0, 100)
                
                categoryBudgetItems.add(
                    CategoryBudgetItem(
                        category = category,
                        spent = amount,
                        budgetLimit = budgetLimit,
                        percentage = percentage
                    )
                )
            }
            
            // Sort by highest percentage spent
            categoryBudgetItems.sortByDescending { it.percentage }
            
            _categoryBudgets.value = categoryBudgetItems
        }
    }
    
    fun saveCategoryBudget(category: Category, amount: Double) {
        viewModelScope.launch {
            val budget = Budget(
                amount = amount,
                category = category,
                month = currentMonth,
                year = currentYear
            )
            
            withContext(Dispatchers.IO) {
                repository.saveBudget(budget)
            }
            
            loadCategoryBudgets()
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        // Clean up observers to prevent memory leaks
        // TransactionsViewModel.budgetUpdateTrigger will be observed with the app lifecycle
    }
}

/**
 * Data class for displaying category budget items in the UI
 */
data class CategoryBudgetItem(
    val category: Category,
    val spent: Double,
    val budgetLimit: Double,
    val percentage: Int
)

/**
 * Data class for budget alert information
 */
data class BudgetAlertInfo(
    val percentage: Int,
    val spent: Double,
    val budget: Double,
    val isExceeded: Boolean
) 