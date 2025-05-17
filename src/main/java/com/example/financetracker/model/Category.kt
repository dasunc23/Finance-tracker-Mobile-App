package com.example.financetracker.model

import androidx.annotation.DrawableRes
import com.example.financetracker.R

/**
 * Enum representing transaction categories
 */
enum class Category(
    val displayName: String,
    @DrawableRes val iconRes: Int
) {
    // Expense Categories
    FOOD("Food & Dining", R.drawable.ic_category_food),
    SHOPPING("Shopping", R.drawable.ic_category_shopping),
    GROCERIES("Groceries", R.drawable.ic_category_groceries),
    TRANSPORT("Transportation", R.drawable.ic_category_transport),
    ENTERTAINMENT("Entertainment", R.drawable.ic_category_entertainment),
    HEALTH("Health & Medical", R.drawable.ic_category_health),
    PHARMACY("Pharmacy", R.drawable.ic_category_health),
    UTILITIES("Utilities", R.drawable.ic_category_utilities),
    ELECTRICITY("Electricity Bill", R.drawable.ic_category_electricity),
    WATER("Water Bill", R.drawable.ic_category_water),
    INTERNET("Internet Bill", R.drawable.ic_category_internet),
    PHONE("Phone Bill", R.drawable.ic_category_phone),
    HOUSING("Housing & Rent", R.drawable.ic_category_housing),
    EDUCATION("Education", R.drawable.ic_category_education),
    GIFTS("Gifts & Donations", R.drawable.ic_category_gifts),
    
    // Income Categories
    SALARY("Salary", R.drawable.ic_category_salary),
    BONUS("Bonus", R.drawable.ic_category_salary),
    INVESTMENT("Investment Income", R.drawable.ic_category_salary),
    
    // Other
    OTHER("Other", R.drawable.ic_category_other);

    companion object {
        /**
         * Get default category for expense
         */
        fun getDefaultExpenseCategory(): Category {
            return OTHER
        }

        /**
         * Get default category for income
         */
        fun getDefaultIncomeCategory(): Category {
            return SALARY
        }
    }
} 