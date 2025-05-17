package com.example.financetracker.ui.transactions

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.financetracker.R
import com.example.financetracker.databinding.ItemTransactionBinding
import com.example.financetracker.model.Transaction
import java.text.SimpleDateFormat
import java.util.Locale

class TransactionAdapter(
    private val onItemClicked: (Transaction) -> Unit,
    private val onEditClicked: (Transaction) -> Unit,
    private val onDeleteClicked: (Transaction) -> Unit
) : ListAdapter<Transaction, TransactionAdapter.TransactionViewHolder>(TransactionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val binding = ItemTransactionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TransactionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TransactionViewHolder(
        private val binding: ItemTransactionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClicked(getItem(position))
                }
            }
            
            binding.btnOptions.setOnClickListener { view ->
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    showPopupMenu(view, getItem(position))
                }
            }
        }
        
        private fun showPopupMenu(view: android.view.View, transaction: Transaction) {
            val popupMenu = PopupMenu(view.context, view)
            popupMenu.inflate(R.menu.menu_transaction_options)
            
            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.action_edit -> {
                        onEditClicked(transaction)
                        true
                    }
                    R.id.action_delete -> {
                        onDeleteClicked(transaction)
                        true
                    }
                    else -> false
                }
            }
            
            popupMenu.show()
        }

        fun bind(transaction: Transaction) {
            // Set transaction title
            binding.tvTitle.text = transaction.title

            // Set transaction date
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            binding.tvDate.text = dateFormat.format(transaction.date)

            // Set transaction amount with proper color
            binding.tvAmount.text = transaction.getFormattedAmount()
            binding.tvAmount.setTextColor(
                ContextCompat.getColor(
                    binding.root.context,
                    if (transaction.isExpense) R.color.expense_red else R.color.income_green
                )
            )

            // Set category icon
            binding.ivCategoryIcon.setImageResource(transaction.category.iconRes)
        }
    }

    class TransactionDiffCallback : DiffUtil.ItemCallback<Transaction>() {
        override fun areItemsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem == newItem
        }
    }
} 