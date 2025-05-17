package com.example.financetracker.ui.budget

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.financetracker.R
import com.example.financetracker.databinding.FragmentBudgetBinding
import com.example.financetracker.model.Category
import com.example.financetracker.ui.transactions.TransactionsViewModel
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.text.NumberFormat
import java.util.Locale

class BudgetFragment : Fragment() {

    private var _binding: FragmentBudgetBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: BudgetViewModel
    private lateinit var transactionsViewModel: TransactionsViewModel
    private lateinit var adapter: CategoryBudgetAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBudgetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Get BudgetViewModel
        viewModel = ViewModelProvider(this)[BudgetViewModel::class.java]
        
        // Get TransactionsViewModel from activity scope to share with AddTransactionDialogFragment
        transactionsViewModel = ViewModelProvider(requireActivity())[TransactionsViewModel::class.java]
        
        // Connect the two view models for live updates
        viewModel.observeTransactionUpdates(transactionsViewModel)
        
        setupRecyclerView()
        setupListeners()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = CategoryBudgetAdapter { categoryBudgetItem ->
            // Open dialog to set category budget
            showSetCategoryBudgetDialog(categoryBudgetItem)
        }
        
        binding.rvCategoryBudgets.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCategoryBudgets.adapter = adapter
    }

    private fun setupListeners() {
        binding.btnSaveBudget.setOnClickListener {
            // Get budget amount and alert threshold
            val budgetAmountText = binding.etBudgetAmount.text.toString()
            val alertThresholdText = binding.etAlertThreshold.text.toString()
            
            if (budgetAmountText.isBlank() || alertThresholdText.isBlank()) {
                Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            val budgetAmount = budgetAmountText.toDoubleOrNull()
            val alertThreshold = alertThresholdText.toIntOrNull()
            
            if (budgetAmount == null || alertThreshold == null) {
                Toast.makeText(requireContext(), "Please enter valid numbers", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            // Save budget
            viewModel.saveBudget(budgetAmount, alertThreshold)
            Toast.makeText(requireContext(), "Budget saved", Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeViewModel() {
        // Observe current month/year
        viewModel.formattedMonthYear.observe(viewLifecycleOwner) { monthYear ->
            binding.tvMonthYear.text = monthYear
        }
        
        // Observe budget
        viewModel.budget.observe(viewLifecycleOwner) { budget ->
            binding.etBudgetAmount.setText(budget.amount.toString())
            binding.etAlertThreshold.setText(budget.alertThreshold.toString())
            
            binding.tvBudgetLimit.text = formatCurrency(budget.amount)
        }
        
        // Observe percentage spent
        viewModel.percentageSpent.observe(viewLifecycleOwner) { percentage ->
            binding.progressBudget.progress = percentage
            binding.tvBudgetPercentage.text = "$percentage%"
            
            // Set progress color based on percentage
            binding.progressBudget.setIndicatorColor(
                resources.getColor(
                    when {
                        percentage >= 100 -> R.color.expense_red
                        percentage >= 80 -> R.color.warning_yellow
                        else -> R.color.primary
                    },
                    null
                )
            )
        }
        
        // Observe total spent
        viewModel.totalSpent.observe(viewLifecycleOwner) { totalSpent ->
            // Update budget used text when total spent or budget changes
            updateBudgetUsedText()
        }
        
        // Observe remaining budget
        viewModel.remainingBudget.observe(viewLifecycleOwner) { remaining ->
            binding.tvBudgetRemaining.text = formatCurrency(remaining)
        }
        
        // Observe category budgets
        viewModel.categoryBudgets.observe(viewLifecycleOwner) { categoryBudgets ->
            adapter.submitList(categoryBudgets)
        }
        
        // Observe budget alert events
        viewModel.showBudgetAlertEvent.observe(viewLifecycleOwner) { alertInfo ->
            alertInfo?.let {
                showBudgetAlertDialog(it)
                viewModel.budgetAlertShown()
            }
        }
    }
    
    private fun updateBudgetUsedText() {
        val totalSpent = viewModel.totalSpent.value ?: 0.0
        val budgetAmount = viewModel.budget.value?.amount ?: 0.0
        
        binding.tvBudgetUsed.text = "${formatCurrency(totalSpent)} / ${formatCurrency(budgetAmount)}"
    }
    
    private fun showBudgetAlertDialog(alertInfo: BudgetAlertInfo) {
        val spentFormatted = formatCurrency(alertInfo.spent)
        val budgetFormatted = formatCurrency(alertInfo.budget)
        
        val title = if (alertInfo.isExceeded) {
            "Budget Exceeded!"
        } else {
            "Budget Alert!"
        }
        
        val message = if (alertInfo.isExceeded) {
            "You have exceeded your budget.\n\nSpent: $spentFormatted\nBudget: $budgetFormatted"
        } else {
            "You have reached ${alertInfo.percentage}% of your budget.\n\nSpent: $spentFormatted\nBudget: $budgetFormatted"
        }
        
        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }
    
    private fun showSetCategoryBudgetDialog(categoryBudgetItem: CategoryBudgetItem) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_set_category_budget, null)
        
        val tilAmount = dialogView.findViewById<TextInputLayout>(R.id.tilCategoryBudgetAmount)
        val etAmount = dialogView.findViewById<TextInputEditText>(R.id.etCategoryBudgetAmount)
        
        etAmount.setText(categoryBudgetItem.budgetLimit.toString())
        
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Set Budget for ${categoryBudgetItem.category.displayName}")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val amountText = etAmount.text.toString()
                val amount = amountText.toDoubleOrNull()
                
                if (amount != null) {
                    viewModel.saveCategoryBudget(categoryBudgetItem.category, amount)
                    Toast.makeText(requireContext(), "Category budget saved", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Please enter a valid amount", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
            
        dialog.show()
    }

    private fun formatCurrency(amount: Double): String {
        return "Rs ${String.format("%.2f", amount)}"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 