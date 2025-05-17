package com.example.financetracker.ui.analytics

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.financetracker.model.Budget
import com.example.financetracker.model.Category
import com.example.financetracker.model.Transaction
import com.example.financetracker.repository.TransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Date

class AnalyticsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TransactionRepository(application)
    
    // Time range for data filtering
    private val _selectedTimeRange = MutableLiveData<TimeRange>()
    val selectedTimeRange: LiveData<TimeRange> = _selectedTimeRange
    
    // Transaction data
    private val _transactions = MutableLiveData<List<Transaction>>()
    val transactions: LiveData<List<Transaction>> = _transactions
    
    // Financial summaries
    private val _totalIncome = MutableLiveData<Double>()
    val totalIncome: LiveData<Double> = _totalIncome
    
    private val _totalExpenses = MutableLiveData<Double>()
    val totalExpenses: LiveData<Double> = _totalExpenses
    
    // Category spending data
    private val _categorySpending = MutableLiveData<Map<Category, Double>>()
    val categorySpending: LiveData<Map<Category, Double>> = _categorySpending
    
    // Monthly overview data
    private val _monthlyData = MutableLiveData<List<MonthlyData>>()
    val monthlyData: LiveData<List<MonthlyData>> = _monthlyData
    
    // Budget data
    private val _budgets = MutableLiveData<List<Budget>>()
    val budgets: LiveData<List<Budget>> = _budgets
    
    private val _budgetVsActual = MutableLiveData<List<BudgetVsActual>>()
    val budgetVsActual: LiveData<List<BudgetVsActual>> = _budgetVsActual
    
    init {
        setTimeRange(TimeRange.THIS_MONTH)
    }
    
    fun setTimeRange(timeRange: TimeRange) {
        _selectedTimeRange.value = timeRange
        loadData(timeRange)
    }
    
    private fun loadData(timeRange: TimeRange) {
        viewModelScope.launch {
            val (startDate, endDate) = getDateRangeForTimeRange(timeRange)
            
            val filteredTransactions = withContext(Dispatchers.IO) {
                repository.getAllTransactions().filter { 
                    it.date in startDate..endDate
                }
            }
            
            _transactions.value = filteredTransactions
            
            calculateFinancialSummaries(filteredTransactions)
            calculateCategorySpending(filteredTransactions)
            
            // Get monthly data based on timeRange
            val monthlyDataList = withContext(Dispatchers.IO) {
                getMonthlyDataForTimeRange(timeRange)
            }
            _monthlyData.value = monthlyDataList
            
            // Load budget data
            loadBudgetData(timeRange)
        }
    }
    
    private suspend fun loadBudgetData(timeRange: TimeRange) {
        val calendar = Calendar.getInstance()
        
        when (timeRange) {
            TimeRange.THIS_MONTH -> {
                val month = calendar.get(Calendar.MONTH)
                val year = calendar.get(Calendar.YEAR)
                loadBudgetsForMonth(month, year)
            }
            TimeRange.LAST_MONTH -> {
                calendar.add(Calendar.MONTH, -1)
                val month = calendar.get(Calendar.MONTH)
                val year = calendar.get(Calendar.YEAR)
                loadBudgetsForMonth(month, year)
            }
            TimeRange.LAST_3_MONTHS -> {
                val budgetActuals = mutableListOf<BudgetVsActual>()
                
                for (i in 0 until 3) {
                    calendar.add(Calendar.MONTH, -i)
                    val month = calendar.get(Calendar.MONTH)
                    val year = calendar.get(Calendar.YEAR)
                    val monthBudgets = withContext(Dispatchers.IO) {
                        getBudgetVsActualForMonth(month, year)
                    }
                    budgetActuals.addAll(monthBudgets)
                    calendar.add(Calendar.MONTH, i) // Reset back for next iteration
                }
                
                _budgetVsActual.value = budgetActuals
            }
            TimeRange.LAST_6_MONTHS, TimeRange.THIS_YEAR -> {
                val budgetActuals = mutableListOf<BudgetVsActual>()
                val months = if (timeRange == TimeRange.LAST_6_MONTHS) 6 else 12
                
                for (i in 0 until months) {
                    calendar.add(Calendar.MONTH, -i)
                    val month = calendar.get(Calendar.MONTH)
                    val year = calendar.get(Calendar.YEAR)
                    val monthBudgets = withContext(Dispatchers.IO) {
                        getBudgetVsActualForMonth(month, year)
                    }
                    budgetActuals.addAll(monthBudgets)
                    calendar.add(Calendar.MONTH, i) // Reset back for next iteration
                }
                
                _budgetVsActual.value = budgetActuals
            }
        }
    }
    
    private suspend fun loadBudgetsForMonth(month: Int, year: Int) {
        val budgets = withContext(Dispatchers.IO) {
            repository.getAllBudgets().filter { 
                it.month == month && it.year == year
            }
        }
        _budgets.value = budgets
        
        val budgetVsActualList = withContext(Dispatchers.IO) {
            getBudgetVsActualForMonth(month, year)
        }
        _budgetVsActual.value = budgetVsActualList
    }
    
    private suspend fun getBudgetVsActualForMonth(month: Int, year: Int): List<BudgetVsActual> {
        val result = mutableListOf<BudgetVsActual>()
        
        // Get budgets for the month
        val budgets = repository.getAllBudgets().filter { 
            it.month == month && it.year == year
        }
        
        // Get transactions for the month
        val transactions = repository.getTransactionsForMonth(month, year)
        
        // Calculate budget vs actual for each budget
        budgets.forEach { budget ->
            val categoryTransactions = if (budget.category != null) {
                transactions.filter { it.category == budget.category && it.isExpense }
            } else {
                transactions.filter { it.isExpense }
            }
            
            val actualSpent = categoryTransactions.sumOf { it.amount }
            val remaining = budget.amount - actualSpent
            val percentageUsed = budget.calculatePercentageSpent(actualSpent)
            
            result.add(
                BudgetVsActual(
                    budget = budget,
                    actualSpent = actualSpent,
                    remaining = remaining,
                    percentageUsed = percentageUsed
                )
            )
        }
        
        // If there's no overall budget yet, create a virtual one for total spending
        if (budgets.none { it.category == null }) {
            val totalExpenses = transactions.filter { it.isExpense }.sumOf { it.amount }
            val totalIncome = transactions.filter { !it.isExpense }.sumOf { it.amount }
            
            if (totalIncome > 0) {
                val virtualBudget = Budget(
                    amount = totalIncome,
                    category = null,
                    month = month,
                    year = year
                )
                
                result.add(
                    BudgetVsActual(
                        budget = virtualBudget,
                        actualSpent = totalExpenses,
                        remaining = totalIncome - totalExpenses,
                        percentageUsed = ((totalExpenses / totalIncome) * 100).toInt().coerceIn(0, 100),
                        isVirtual = true
                    )
                )
            }
        }
        
        return result
    }
    
    private fun calculateFinancialSummaries(transactions: List<Transaction>) {
        val income = transactions.filter { !it.isExpense }.sumOf { it.amount }
        val expenses = transactions.filter { it.isExpense }.sumOf { it.amount }
        
        _totalIncome.value = income
        _totalExpenses.value = expenses
    }
    
    private fun calculateCategorySpending(transactions: List<Transaction>) {
        val expensesByCategory = transactions
            .filter { it.isExpense }
            .groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
        
        _categorySpending.value = expensesByCategory
    }
    
    private suspend fun getMonthlyDataForTimeRange(timeRange: TimeRange): List<MonthlyData> {
        val result = mutableListOf<MonthlyData>()
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)
        
        val monthsToFetch = when (timeRange) {
            TimeRange.THIS_MONTH -> 1
            TimeRange.LAST_MONTH -> 1
            TimeRange.LAST_3_MONTHS -> 3
            TimeRange.LAST_6_MONTHS -> 6
            TimeRange.THIS_YEAR -> 12
        }
        
        for (i in 0 until monthsToFetch) {
            if (timeRange == TimeRange.LAST_MONTH) {
                calendar.add(Calendar.MONTH, -1)
            } else if (i > 0) {
                calendar.add(Calendar.MONTH, -1)
            }
            
            val month = calendar.get(Calendar.MONTH)
            val year = calendar.get(Calendar.YEAR)
            
            val income = repository.getTotalIncomeForMonth(month, year)
            val expenses = repository.getTotalExpensesForMonth(month, year)
            
            val monthName = when (month) {
                Calendar.JANUARY -> "Jan"
                Calendar.FEBRUARY -> "Feb"
                Calendar.MARCH -> "Mar"
                Calendar.APRIL -> "Apr"
                Calendar.MAY -> "May"
                Calendar.JUNE -> "Jun"
                Calendar.JULY -> "Jul"
                Calendar.AUGUST -> "Aug"
                Calendar.SEPTEMBER -> "Sep"
                Calendar.OCTOBER -> "Oct"
                Calendar.NOVEMBER -> "Nov"
                Calendar.DECEMBER -> "Dec"
                else -> "Unknown"
            }
            
            result.add(
                MonthlyData(
                    month = month,
                    year = year,
                    monthName = monthName,
                    income = income,
                    expenses = expenses
                )
            )
        }
        
        return result
    }
    
    private fun getDateRangeForTimeRange(timeRange: TimeRange): Pair<Date, Date> {
        val calendar = Calendar.getInstance()
        val endDate = calendar.time
        
        when (timeRange) {
            TimeRange.THIS_MONTH -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
            }
            TimeRange.LAST_MONTH -> {
                calendar.add(Calendar.MONTH, -1)
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                
                val lastDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                calendar.set(Calendar.DAY_OF_MONTH, lastDay)
                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                calendar.set(Calendar.SECOND, 59)
                
                return Pair(calendar.time, calendar.time)
            }
            TimeRange.LAST_3_MONTHS -> {
                calendar.add(Calendar.MONTH, -3)
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
            }
            TimeRange.LAST_6_MONTHS -> {
                calendar.add(Calendar.MONTH, -6)
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
            }
            TimeRange.THIS_YEAR -> {
                calendar.set(Calendar.MONTH, Calendar.JANUARY)
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
            }
        }
        
        val startDate = calendar.time
        return Pair(startDate, endDate)
    }
    
    enum class TimeRange {
        THIS_MONTH,
        LAST_MONTH,
        LAST_3_MONTHS,
        LAST_6_MONTHS,
        THIS_YEAR
    }
    
    data class MonthlyData(
        val month: Int,
        val year: Int,
        val monthName: String,
        val income: Double,
        val expenses: Double
    )
    
    data class BudgetVsActual(
        val budget: Budget,
        val actualSpent: Double,
        val remaining: Double,
        val percentageUsed: Int,
        val isVirtual: Boolean = false
    )
} 