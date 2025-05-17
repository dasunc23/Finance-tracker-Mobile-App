package com.example.financetracker.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.financetracker.R
import com.example.financetracker.databinding.ActivityMainBinding
import com.example.financetracker.repository.TransactionRepository
import com.example.financetracker.ui.analytics.AnalyticsFragment
import com.example.financetracker.ui.auth.LoginActivity
import com.example.financetracker.ui.budget.BudgetFragment
import com.example.financetracker.ui.profile.ProfileFragment
import com.example.financetracker.ui.transactions.AddTransactionDialogFragment
import com.example.financetracker.ui.transactions.TransactionsFragment
import com.example.financetracker.util.NotificationHelper
import com.example.financetracker.util.PreferenceHelper
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.*

class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var preferenceHelper: PreferenceHelper
    private lateinit var notificationHelper: NotificationHelper
    private lateinit var repository: TransactionRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        preferenceHelper = PreferenceHelper(this)
        notificationHelper = NotificationHelper(this)
        repository = TransactionRepository(this)
        
        // Check if user is logged in, if not redirect to login screen
        if (!preferenceHelper.isLoggedIn()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }
        
        setSupportActionBar(binding.toolbar)
        
        // Set up bottom navigation
        binding.bottomNavView.setOnNavigationItemSelectedListener(this)
        
        // Load default fragment
        if (savedInstanceState == null) {
            loadFragment(TransactionsFragment())
            binding.bottomNavView.selectedItemId = R.id.navigation_transactions
        }
        
        // Set up FAB for adding transactions
        binding.fabAddTransaction.setOnClickListener {
            showAddTransactionDialog()
        }
        
        // Setup budget alerts
        setupBudgetAlerts()
    }
    
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val fragment: Fragment = when (item.itemId) {
            R.id.navigation_transactions -> {
                binding.fabAddTransaction.show()
                TransactionsFragment()
            }
            R.id.navigation_budget -> {
                binding.fabAddTransaction.hide()
                BudgetFragment()
            }
            R.id.navigation_analytics -> {
                binding.fabAddTransaction.hide()
                AnalyticsFragment()
            }
            R.id.navigation_profile -> {
                binding.fabAddTransaction.hide()
                ProfileFragment()
            }
            else -> return false
        }
        
        return loadFragment(fragment)
    }
    
    private fun loadFragment(fragment: Fragment): Boolean {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
        return true
    }
    
    private fun showAddTransactionDialog() {
        val dialog = AddTransactionDialogFragment()
        dialog.show(supportFragmentManager, "AddTransactionDialog")
    }
    
    private fun setupBudgetAlerts() {
        // Check budget thresholds for current month
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)

        // Get current budget and expenses
        val budget = repository.getBudgetForMonth(currentMonth, currentYear)
        val expenses = repository.getTotalExpensesForMonth(currentMonth, currentYear)

        // If budget exists and threshold is exceeded, show notification
        if (budget != null && expenses > 0) {
            if (budget.isThresholdExceeded(expenses) || budget.isBudgetExceeded(expenses)) {
                val percentage = ((expenses / budget.amount) * 100).toInt().coerceIn(0, 100)
                notificationHelper.showBudgetWarningNotification(percentage)
            }
        }
        
        // Schedule daily reminder if enabled in settings
        if (preferenceHelper.isDailyReminderEnabled()) {
            notificationHelper.scheduleDailyReminder(
                preferenceHelper.getReminderHour(),
                preferenceHelper.getReminderMinute()
            )
        }
    }
} 