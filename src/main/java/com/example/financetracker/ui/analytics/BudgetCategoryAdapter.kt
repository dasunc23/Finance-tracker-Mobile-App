package com.example.financetracker.ui.analytics

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.financetracker.R
import com.example.financetracker.databinding.ItemBudgetCategoryBinding
import java.text.NumberFormat
import java.util.Locale

class BudgetCategoryAdapter : 
    ListAdapter<AnalyticsViewModel.BudgetVsActual, BudgetCategoryAdapter.BudgetViewHolder>(BudgetDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BudgetViewHolder {
        val binding = ItemBudgetCategoryBinding.inflate(
            LayoutInflater.from(parent.context), 
            parent, 
            false
        )
        return BudgetViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BudgetViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class BudgetViewHolder(
        private val binding: ItemBudgetCategoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: AnalyticsViewModel.BudgetVsActual) {
            val context = binding.root.context
            
            // Set category icon and name
            item.budget.category?.let { category ->
                binding.ivCategoryIcon.setImageResource(category.iconRes)
                binding.tvCategoryName.text = category.displayName
            } ?: run {
                binding.ivCategoryIcon.setImageResource(R.drawable.ic_category_other)
                binding.tvCategoryName.text = "Overall Budget"
            }
            
            // Format currency values
            val spentText = "Rs ${String.format("%.2f", item.actualSpent)}"
            val budgetText = "Rs ${String.format("%.2f", item.budget.amount)}"
            binding.tvBudgetValues.text = "$spentText / $budgetText"
            
            // Set percentage
            binding.tvPercentUsed.text = "${item.percentageUsed}%"
            
            // Set progress
            binding.progressBudget.progress = item.percentageUsed
            
            // Set color based on budget status
            val progressColor = when {
                item.percentageUsed >= 100 -> R.color.expense_red
                item.percentageUsed >= 80 -> R.color.warning_yellow
                else -> R.color.income_green
            }
            binding.progressBudget.progressTintList = 
                ContextCompat.getColorStateList(context, progressColor)
        }
    }

    class BudgetDiffCallback : DiffUtil.ItemCallback<AnalyticsViewModel.BudgetVsActual>() {
        override fun areItemsTheSame(
            oldItem: AnalyticsViewModel.BudgetVsActual,
            newItem: AnalyticsViewModel.BudgetVsActual
        ): Boolean {
            return oldItem.budget.id == newItem.budget.id
        }

        override fun areContentsTheSame(
            oldItem: AnalyticsViewModel.BudgetVsActual,
            newItem: AnalyticsViewModel.BudgetVsActual
        ): Boolean {
            return oldItem == newItem
        }
    }
} 