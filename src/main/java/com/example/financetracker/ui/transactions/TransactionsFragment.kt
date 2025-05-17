package com.example.financetracker.ui.transactions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.financetracker.R
import com.example.financetracker.databinding.FragmentTransactionsBinding
import com.example.financetracker.model.Transaction
import java.text.NumberFormat
import java.util.Locale

class TransactionsFragment : Fragment() {

    private var _binding: FragmentTransactionsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: TransactionsViewModel
    private lateinit var adapter: TransactionAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransactionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Use activity scope for the ViewModel to share it with the AddTransactionDialogFragment
        viewModel = ViewModelProvider(requireActivity())[TransactionsViewModel::class.java]
        
        setupRecyclerView()
        observeViewModel()
        
        // Refresh transactions list when view is created
        viewModel.loadTransactions()
    }

    private fun setupRecyclerView() {
        adapter = TransactionAdapter(
            onItemClicked = { transaction ->
                // Handle transaction click (view details)
                openTransactionDetail(transaction)
            },
            onEditClicked = { transaction ->
                // Handle edit click
                editTransaction(transaction)
            },
            onDeleteClicked = { transaction ->
                // Handle delete click
                showDeleteConfirmationDialog(transaction)
            }
        )
        
        binding.rvTransactions.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTransactions.adapter = adapter
    }

    private fun observeViewModel() {
        // Observe transactions
        viewModel.transactions.observe(viewLifecycleOwner) { transactions ->
            adapter.submitList(transactions)
            
            // Show/hide empty state
            if (transactions.isEmpty()) {
                binding.rvTransactions.visibility = View.GONE
                binding.tvNoTransactions.visibility = View.VISIBLE
            } else {
                binding.rvTransactions.visibility = View.VISIBLE
                binding.tvNoTransactions.visibility = View.GONE
            }
        }
        
        // Observe balance
        viewModel.balance.observe(viewLifecycleOwner) { balance ->
            binding.tvBalance.text = formatCurrency(balance)
        }
        
        // Observe income
        viewModel.income.observe(viewLifecycleOwner) { income ->
            binding.tvIncome.text = formatCurrency(income, true)
        }
        
        // Observe expenses
        viewModel.expenses.observe(viewLifecycleOwner) { expenses ->
            binding.tvExpenses.text = formatCurrency(expenses, false)
        }
    }

    private fun formatCurrency(amount: Double, isIncome: Boolean = true): String {
        val prefix = if (isIncome) "+Rs " else "-Rs "
        return "$prefix${"%.2f".format(amount)}"
    }

    private fun openTransactionDetail(transaction: Transaction) {
        // This would be handled by showing a dialog with transaction details
        editTransaction(transaction)
    }
    
    private fun editTransaction(transaction: Transaction) {
        val bundle = Bundle().apply {
            putString("transactionId", transaction.id)
        }
        
        // Open the add/edit transaction dialog with the transaction ID
        val dialog = AddTransactionDialogFragment().apply {
            arguments = bundle
        }
        dialog.show(parentFragmentManager, "EditTransactionDialog")
    }
    
    private fun showDeleteConfirmationDialog(transaction: Transaction) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Transaction")
            .setMessage("Are you sure you want to delete this transaction?")
            .setPositiveButton("Delete") { _, _ ->
                deleteTransaction(transaction)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun deleteTransaction(transaction: Transaction) {
        viewModel.deleteTransaction(transaction.id)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 