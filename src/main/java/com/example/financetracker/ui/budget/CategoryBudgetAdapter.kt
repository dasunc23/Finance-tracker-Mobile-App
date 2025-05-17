package com.example.financetracker.ui.budget

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.financetracker.R
import com.example.financetracker.databinding.ItemCategoryBudgetBinding
import java.text.NumberFormat
import java.util.Locale

class CategoryBudgetAdapter(
    private val onItemClicked: (CategoryBudgetItem) -> Unit
) : ListAdapter<CategoryBudgetItem, CategoryBudgetAdapter.CategoryBudgetViewHolder>(CategoryBudgetDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryBudgetViewHolder {
        val binding = ItemCategoryBudgetBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CategoryBudgetViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryBudgetViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CategoryBudgetViewHolder(
        private val binding: ItemCategoryBudgetBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClicked(getItem(position))
                }
            }
        }

        fun bind(item: CategoryBudgetItem) {
            // Set category name
            binding.tvCategoryName.text = item.category.displayName
            
            // Set category icon
            binding.ivCategoryIcon.setImageResource(item.category.iconRes)
            
            // Set progress bar
            binding.progressCategory.apply {
                progress = item.percentage
                
                // Set color based on percentage
                setIndicatorColor(
                    ContextCompat.getColor(
                        context,
                        when {
                            item.percentage >= 100 -> R.color.expense_red
                            item.percentage >= 80 -> R.color.warning_yellow
                            else -> R.color.primary
                        }
                    )
                )
            }
            
            // Set budget info text
            val budgetInfo = "Rs ${String.format("%.2f", item.spent)}/Rs ${String.format("%.2f", item.budgetLimit)}"
            binding.tvBudgetInfo.text = budgetInfo
            
            // Set text color based on percentage
            binding.tvBudgetInfo.setTextColor(
                ContextCompat.getColor(
                    binding.root.context,
                    when {
                        item.percentage >= 100 -> R.color.expense_red
                        item.percentage >= 80 -> R.color.warning_yellow
                        else -> R.color.on_background
                    }
                )
            )
        }
    }

    class CategoryBudgetDiffCallback : DiffUtil.ItemCallback<CategoryBudgetItem>() {
        override fun areItemsTheSame(oldItem: CategoryBudgetItem, newItem: CategoryBudgetItem): Boolean {
            return oldItem.category == newItem.category
        }

        override fun areContentsTheSame(oldItem: CategoryBudgetItem, newItem: CategoryBudgetItem): Boolean {
            return oldItem == newItem
        }
    }
} 