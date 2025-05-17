package com.example.financetracker.ui.profile

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.financetracker.R
import com.example.financetracker.databinding.FragmentProfileBinding
import com.example.financetracker.repository.TransactionRepository
import com.example.financetracker.ui.auth.LoginActivity
import com.example.financetracker.ui.transactions.TransactionsViewModel
import com.example.financetracker.util.NotificationHelper
import com.example.financetracker.util.PreferenceHelper
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var preferenceHelper: PreferenceHelper
    private lateinit var notificationHelper: NotificationHelper
    private lateinit var transactionRepository: TransactionRepository
    private lateinit var transactionsViewModel: TransactionsViewModel
    
    companion object {
        private const val REQUEST_IMAGE_PICK = 100
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        preferenceHelper = PreferenceHelper(requireContext())
        notificationHelper = NotificationHelper(requireContext())
        transactionRepository = TransactionRepository(requireContext())
        
        // Initialize ViewModel for transaction data
        transactionsViewModel = ViewModelProvider(this)[TransactionsViewModel::class.java]
        
        setupProfile()
        setupFinancialSummary()
        setupSettings()
        setupListeners()
        
        // Observe transaction data updates
        observeViewModel()
    }
    
    private fun setupProfile() {
        // Display user info
        val userName = preferenceHelper.getUserName()
        binding.tvUserName.text = userName
        
        // Set hello message
        binding.tvHelloMessage.text = "Hello, $userName! Welcome back."
        
        // Set profile image if available
        // Note: This would require implementation of image storage and retrieval
    }
    
    private fun setupListeners() {
        // Profile picture edit button
        binding.btnEditProfilePicture.setOnClickListener {
            openImagePicker()
        }
        
        // Name edit button
        binding.btnEditName.setOnClickListener {
            showEditNameDialog()
        }
        
        // Dark mode switch
        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            preferenceHelper.setDarkMode(isChecked)
            // In a real app, you'd apply the theme change here
        }
        
        // Notifications switch
        binding.switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            preferenceHelper.setNotificationsEnabled(isChecked)
            preferenceHelper.setDailyReminderEnabled(isChecked)
            
            if (isChecked) {
                notificationHelper.scheduleDailyReminder(
                    preferenceHelper.getReminderHour(),
                    preferenceHelper.getReminderMinute()
                )
            } else {
                notificationHelper.cancelDailyReminder()
            }
        }
        
        // About section
        binding.tvAbout.setOnClickListener {
            showAboutDialog()
        }
        
        // Logout button
        binding.btnLogout.setOnClickListener {
            showLogoutConfirmationDialog()
        }
    }
    
    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_IMAGE_PICK)
    }
    
    private fun showEditNameDialog() {
        val editText = EditText(context)
        editText.setText(binding.tvUserName.text)
        
        AlertDialog.Builder(requireContext())
            .setTitle("Edit Name")
            .setView(editText)
            .setPositiveButton("Save") { _, _ ->
                val newName = editText.text.toString().trim()
                if (newName.isNotEmpty()) {
                    updateUserName(newName)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun updateUserName(newName: String) {
        // Update UI
        binding.tvUserName.text = newName
        binding.tvHelloMessage.text = "Hello, $newName! Welcome back."
        
        // Save to preferences
        preferenceHelper.saveUserDetails(newName, preferenceHelper.getUserEmail())
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                // Set the selected image to the profile image view
                binding.ivProfilePicture.setImageURI(uri)
                
                // In a real app, you would save the image to storage
                // and store the reference in preferences or a database
            }
        }
    }
    
    private fun setupFinancialSummary() {
        // Load financial data
        updateFinancialCards()
    }
    
    private fun updateFinancialCards() {
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)
        
        // Get total income, expenses, and budget
        val income = transactionRepository.getTotalIncomeForMonth(currentMonth, currentYear)
        val expenses = transactionRepository.getTotalExpensesForMonth(currentMonth, currentYear)
        
        // Get monthly budget from preferences or calculate from transactions
        val monthlyBudget = preferenceHelper.getMonthlyBudget().toDouble()
        val budgetLeft = if (monthlyBudget > 0) {
            monthlyBudget - expenses
        } else {
            income - expenses // If no budget set, use income as budget
        }
        
        // Update the UI with financial data
        binding.tvTotalIncome.text = formatCurrency(income)
        binding.tvTotalExpenses.text = formatCurrency(expenses)
        binding.tvBudgetLeft.text = formatCurrency(budgetLeft)
        
        // Update budget progress bar
        if (monthlyBudget > 0) {
            val progress = ((expenses / monthlyBudget) * 100).toInt().coerceAtMost(100)
            binding.progressBudget.progress = progress
        } else if (income > 0) {
            val progress = ((expenses / income) * 100).toInt().coerceAtMost(100)
            binding.progressBudget.progress = progress
        } else {
            binding.progressBudget.progress = 0
        }
    }
    
    private fun formatCurrency(amount: Double): String {
        return "Rs ${String.format("%.2f", amount)}"
    }
    
    private fun observeViewModel() {
        // Update UI when transaction data changes
        transactionsViewModel.balance.observe(viewLifecycleOwner) {
            updateFinancialCards()
        }
    }
    
    private fun setupSettings() {
        // Set up switch states based on preferences
        binding.switchDarkMode.isChecked = preferenceHelper.isDarkModeEnabled()
        binding.switchNotifications.isChecked = preferenceHelper.areNotificationsEnabled()
    }
    
    private fun showAboutDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.about))
            .setMessage("Finance Tracker App v1.0\n\nA simple app to track your expenses and budget.")
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }
    
    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _, _ ->
                logout()
            }
            .setNegativeButton("No", null)
            .show()
    }
    
    private fun logout() {
        // Cancel daily reminders
        notificationHelper.cancelDailyReminder()
        
        // Clear user session
        preferenceHelper.setLoggedIn(false)
        
        // Navigate to login screen
        startActivity(Intent(requireContext(), LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        activity?.finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 