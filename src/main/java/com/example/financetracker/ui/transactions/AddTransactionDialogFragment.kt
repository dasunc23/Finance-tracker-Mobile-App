package com.example.financetracker.ui.transactions

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.example.financetracker.R
import com.example.financetracker.databinding.DialogAddTransactionBinding
import com.example.financetracker.model.Category
import com.example.financetracker.model.Transaction
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AddTransactionDialogFragment : DialogFragment() {

    private var _binding: DialogAddTransactionBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: TransactionsViewModel
    private var selectedDate: Date = Date()
    private var transactionId: String? = null
    private var selectedCategory: Category? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Theme_FinanceTracker_PopupOverlay)
        
        arguments?.let {
            transactionId = it.getString("transactionId")
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogAddTransactionBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        viewModel = ViewModelProvider(requireActivity())[TransactionsViewModel::class.java]
        
        setupViews()
        setupCategorySpinner()
        setupListeners()
        
        // If editing existing transaction, load its data
        if (transactionId != null) {
            binding.tvDialogTitle.text = getString(R.string.edit_transaction)
            loadTransactionData()
        }
    }

    private fun setupViews() {
        // Set current date as default
        updateDateDisplay()
    }
    
    private fun setupCategorySpinner() {
        try {
            // Get all categories
            val categories = Category.values()
            
            // Create simple adapter (using string array adapter and string array)
            val categoryNames = categories.map { it.displayName }.toTypedArray()
            val adapter = ArrayAdapter(
                requireContext(), 
                android.R.layout.simple_spinner_item,
                categoryNames
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            
            // Set the adapter
            binding.spinnerCategory.adapter = adapter
            
            // Handle selection
            binding.spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    val categoryName = parent?.getItemAtPosition(position) as String
                    selectedCategory = categories.find { it.displayName == categoryName }
                }
                
                override fun onNothingSelected(parent: AdapterView<*>?) {
                    selectedCategory = null
                }
            }
            
            // Set default category
            updateDefaultCategory()
            
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error setting up categories: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun updateDefaultCategory() {
        val isExpense = binding.rbExpense.isChecked
        val defaultCategory = if (isExpense) {
            Category.getDefaultExpenseCategory()
        } else {
            Category.getDefaultIncomeCategory()
        }
        
        // Find index of default category
        val adapter = binding.spinnerCategory.adapter
        for (i in 0 until adapter.count) {
            val categoryName = adapter.getItem(i) as String
            if (categoryName == defaultCategory.displayName) {
                binding.spinnerCategory.setSelection(i)
                selectedCategory = defaultCategory
                break
            }
        }
        
        // Update radio button listener
        binding.radioGroupType.setOnCheckedChangeListener { _, _ ->
            updateDefaultCategory()
        }
    }
    
    private fun setupListeners() {
        // Date picker
        binding.etDate.setOnClickListener {
            showDatePicker()
        }
        
        binding.tilDate.setEndIconOnClickListener {
            showDatePicker()
        }
        
        // Save button
        binding.btnSave.setOnClickListener {
            saveTransaction()
        }
        
        // Cancel button
        binding.btnCancel.setOnClickListener {
            dismiss()
        }
    }
    
    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        calendar.time = selectedDate
        
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                selectedDate = calendar.time
                updateDateDisplay()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        
        datePickerDialog.show()
    }
    
    private fun updateDateDisplay() {
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        binding.etDate.setText(dateFormat.format(selectedDate))
    }
    
    private fun loadTransactionData() {
        // Find transaction by ID
        val transaction = viewModel.getTransactionById(transactionId!!)
        
        if (transaction != null) {
            binding.etTitle.setText(transaction.title)
            binding.etAmount.setText(transaction.amount.toString())
            binding.etNote.setText(transaction.note)
            selectedDate = transaction.date
            updateDateDisplay()
            
            // Set transaction type
            if (transaction.isExpense) {
                binding.rbExpense.isChecked = true
            } else {
                binding.rbIncome.isChecked = true
            }
            
            // Set category in spinner
            selectCategoryInSpinner(transaction.category)
        }
    }
    
    private fun selectCategoryInSpinner(category: Category) {
        val adapter = binding.spinnerCategory.adapter
        for (i in 0 until adapter.count) {
            val categoryName = adapter.getItem(i) as String
            if (categoryName == category.displayName) {
                binding.spinnerCategory.setSelection(i)
                selectedCategory = category
                break
            }
        }
    }
    
    private fun saveTransaction() {
        val title = binding.etTitle.text.toString().trim()
        val amountStr = binding.etAmount.text.toString().trim() 
        val note = binding.etNote.text.toString().trim()
        val isExpense = binding.rbExpense.isChecked
        
        // Validate inputs
        if (title.isEmpty() || amountStr.isEmpty() || selectedCategory == null) {
            Toast.makeText(requireContext(), "Please fill all required fields", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Parse amount 
        val amount = try {
            amountStr.toDouble()
        } catch (e: NumberFormatException) {
            Toast.makeText(requireContext(), "Invalid amount", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Create transaction object
        val transaction = Transaction(
            id = transactionId ?: "",
            title = title,
            amount = amount,
            date = selectedDate,
            category = selectedCategory!!,
            isExpense = isExpense,
            note = note
        )
        
        // Save transaction
        if (transactionId != null) {
            viewModel.updateTransaction(transaction)
        } else {
            viewModel.addTransaction(transaction)
        }
        
        // Force immediate refresh of transactions list
        viewModel.loadTransactions()
        
        dismiss()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 