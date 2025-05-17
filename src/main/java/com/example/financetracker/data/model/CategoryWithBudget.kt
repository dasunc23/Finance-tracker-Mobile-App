package com.example.financetracker.data.model

import com.example.financetracker.model.Budget
import com.example.financetracker.model.Category

/**
 * Data class representing a category with its associated budget and spending information
 */
data class CategoryWithBudget(
    val category: CategoryInfo,
    val budget: Budget? = null,
    val totalSpent: Double? = 0.0
) {
    /**
     * Data class for category information
     */
    data class CategoryInfo(
        val id: String,
        val name: String,
        val iconRes: Int
    )
    
    /**
     * Convert from Category to CategoryWithBudget
     */
    companion object {
        fun fromCategory(
            category: Category, 
            budget: Budget? = null, 
            totalSpent: Double? = 0.0
        ): CategoryWithBudget {
            return CategoryWithBudget(
                category = CategoryInfo(
                    id = category.name,
                    name = category.displayName,
                    iconRes = category.iconRes
                ),
                budget = budget,
                totalSpent = totalSpent
            )
        }
    }
} 