package com.example.financetracker.ui.transactions

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.financetracker.model.Budget
import com.example.financetracker.model.Category
import com.example.financetracker.model.Transaction
import com.example.financetracker.repository.TransactionRepository
import com.example.financetracker.util.NotificationHelper
import java.util.Calendar
import java.util.Date
import java.util.concurrent.Executors

class TransactionsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TransactionRepository(application)
    private val notificationHelper = NotificationHelper(application)
    private val executor = Executors.newSingleThreadExecutor()
    
    // LiveData for budget updates - will be observed by BudgetViewModel
    private val _budgetUpdateTrigger = MutableLiveData<Long>()
    val budgetUpdateTrigger: LiveData<Long> = _budgetUpdateTrigger
    
    private val _transactions = MutableLiveData<List<Transaction>>()
    val transactions: LiveData<List<Transaction>> = _transactions
    
    private val _income = MutableLiveData<Double>()
    val income: LiveData<Double> = _income
    
    private val _expenses = MutableLiveData<Double>()
    val expenses: LiveData<Double> = _expenses
    
    private val _balance = MutableLiveData<Double>()
    val balance: LiveData<Double> = _balance
    
    private val calendar = Calendar.getInstance()
    private val currentMonth = calendar.get(Calendar.MONTH)
    private val currentYear = calendar.get(Calendar.YEAR)
    
    init {
        loadTransactions()
    }
    
    fun loadTransactions() {
        executor.execute {
            val result = repository.getTransactionsForMonth(currentMonth, currentYear)
            _transactions.postValue(result.sortedByDescending { it.date })
            updateFinancialSummary()
        }
    }
    
    private fun updateFinancialSummary() {
        executor.execute {
            val income = repository.getTotalIncomeForMonth(currentMonth, currentYear)
            val expenses = repository.getTotalExpensesForMonth(currentMonth, currentYear)
            
            _income.postValue(income)
            _expenses.postValue(expenses)
            _balance.postValue(income - expenses)
            
            // Check budget threshold after updating expenses
            checkBudgetThreshold(expenses)
            
            // Trigger budget update
            notifyBudgetUpdate()
        }
    }
    
    // Check if expenses have exceeded the budget threshold and show notification
    private fun checkBudgetThreshold(expenses: Double) {
        executor.execute {
            val budget = repository.getBudgetForMonth(currentMonth, currentYear)
            
            if (budget != null && expenses > 0) {
                if (budget.isThresholdExceeded(expenses) || budget.isBudgetExceeded(expenses)) {
                    val percentage = ((expenses / budget.amount) * 100).toInt().coerceIn(0, 100)
                    
                    // Show popup notification
                    notificationHelper.showBudgetWarningNotification(percentage)
                }
            }
        }
    }
    
    // Notify that budget data should be updated
    private fun notifyBudgetUpdate() {
        _budgetUpdateTrigger.postValue(System.currentTimeMillis())
    }
    
    // Get transaction by ID
    fun getTransactionById(transactionId: String): Transaction? {
        return repository.getAllTransactions().find { it.id == transactionId }
    }
    
    // Add a transaction object
    fun addTransaction(transaction: Transaction) {
        executor.execute {
            repository.addTransaction(transaction)
            
            // Immediately load and update the transactions list
            val result = repository.getTransactionsForMonth(currentMonth, currentYear)
            _transactions.postValue(result.sortedByDescending { it.date })
            updateFinancialSummary()
        }
    }
    
    fun addTransaction(
        title: String,
        amount: Double,
        category: Category,
        date: Date,
        isExpense: Boolean,
        note: String
    ) {
        val transaction = Transaction(
            title = title,
            amount = amount,
            category = category,
            date = date,
            isExpense = isExpense,
            note = note
        )
        
        executor.execute {
            repository.addTransaction(transaction)
            
            // Immediately load and update the transactions list
            val result = repository.getTransactionsForMonth(currentMonth, currentYear)
            _transactions.postValue(result.sortedByDescending { it.date })
            updateFinancialSummary()
        }
    }
    
    fun updateTransaction(transaction: Transaction) {
        executor.execute {
            repository.updateTransaction(transaction)
            
            // Immediately load and update the transactions list
            val result = repository.getTransactionsForMonth(currentMonth, currentYear)
            _transactions.postValue(result.sortedByDescending { it.date })
            updateFinancialSummary()
        }
    }
    
    fun deleteTransaction(transactionId: String) {
        executor.execute {
            repository.deleteTransaction(transactionId)
            loadTransactions()
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        executor.shutdown()
    }
} 