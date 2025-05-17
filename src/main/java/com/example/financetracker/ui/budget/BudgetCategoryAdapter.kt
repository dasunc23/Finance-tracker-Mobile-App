package com.example.financetracker.ui.budget

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.financetracker.data.model.CategoryWithBudget
import com.example.financetracker.databinding.ItemBudgetCategoryBinding
import java.text.NumberFormat
import java.util.Locale

class BudgetCategoryAdapter(
    private val onItemClick: (CategoryWithBudget) -> Unit
) : ListAdapter<CategoryWithBudget, BudgetCategoryAdapter.BudgetCategoryViewHolder>(BudgetCategoryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BudgetCategoryViewHolder {
        val binding = ItemBudgetCategoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return BudgetCategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BudgetCategoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class BudgetCategoryViewHolder(
        private val binding: ItemBudgetCategoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }
        }

        fun bind(categoryWithBudget: CategoryWithBudget) {
            binding.apply {
                tvCategoryName.text = categoryWithBudget.category.name
                
                val budgetAmount = categoryWithBudget.budget?.amount ?: 0.0
                val spentAmount = categoryWithBudget.totalSpent ?: 0.0
                
                // Format as "Spent / Budget"
                val spentText = "Rs ${String.format("%.2f", spentAmount)}"
                val budgetText = "Rs ${String.format("%.2f", budgetAmount)}"
                tvBudgetValues.text = "$spentText / $budgetText"
                
                // Calculate progress
                val progress = if (budgetAmount > 0) {
                    ((spentAmount / budgetAmount) * 100).toInt().coerceIn(0, 100)
                } else {
                    0
                }
                
                progressBudget.progress = progress
                tvPercentUsed.text = "$progress%"
                
                // Change color based on spending
                val context = root.context
                if (spentAmount > budgetAmount && budgetAmount > 0) {
                    progressBudget.progressTintList = android.content.res.ColorStateList.valueOf(
                        context.getColor(android.R.color.holo_red_dark)
                    )
                } else if (spentAmount > budgetAmount * 0.8 && budgetAmount > 0) {
                    progressBudget.progressTintList = android.content.res.ColorStateList.valueOf(
                        context.getColor(android.R.color.holo_orange_dark)
                    )
                } else {
                    progressBudget.progressTintList = android.content.res.ColorStateList.valueOf(
                        context.getColor(android.R.color.holo_green_dark)
                    )
                }
                
                // Set icon if available
                categoryWithBudget.category.iconRes?.let { iconRes ->
                    ivCategoryIcon.setImageResource(iconRes)
                    ivCategoryIcon.visibility = android.view.View.VISIBLE
                } ?: run {
                    ivCategoryIcon.visibility = android.view.View.GONE
                }
            }
        }
    }
}

class BudgetCategoryDiffCallback : DiffUtil.ItemCallback<CategoryWithBudget>() {
    override fun areItemsTheSame(oldItem: CategoryWithBudget, newItem: CategoryWithBudget): Boolean {
        return oldItem.category.id == newItem.category.id
    }

    override fun areContentsTheSame(oldItem: CategoryWithBudget, newItem: CategoryWithBudget): Boolean {
        return oldItem == newItem
    }
} 