package com.example.financetracker.util

import android.content.Context
import android.content.SharedPreferences
import java.security.MessageDigest

/**
 * Helper class to manage SharedPreferences for the app
 */
class PreferenceHelper(context: Context) {
    
    companion object {
        private const val PREF_NAME = "finance_tracker_preferences"
        private const val IS_FIRST_TIME_LAUNCH = "IsFirstTimeLaunch"
        private const val IS_LOGGED_IN = "IsLoggedIn"
        private const val USER_NAME = "UserName"
        private const val USER_EMAIL = "UserEmail"
        private const val USER_PASSWORD = "UserPassword"
        private const val MONTHLY_BUDGET = "MonthlyBudget"
        private const val DARK_MODE = "DarkMode"
        private const val NOTIFICATIONS_ENABLED = "NotificationsEnabled"
        private const val DAILY_REMINDER_ENABLED = "DailyReminderEnabled"
        private const val REMINDER_HOUR = "ReminderHour"
        private const val REMINDER_MINUTE = "ReminderMinute"
    }
    
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = sharedPreferences.edit()
    
    /**
     * Set first time launch flag to false after first app launch
     */
    fun setFirstTimeLaunch(isFirstTime: Boolean) {
        editor.putBoolean(IS_FIRST_TIME_LAUNCH, isFirstTime)
        editor.commit()
    }
    
    /**
     * Check if it's the first time the app is launched
     */
    fun isFirstTimeLaunch(): Boolean {
        return sharedPreferences.getBoolean(IS_FIRST_TIME_LAUNCH, true)
    }
    
    /**
     * Set user login status
     */
    fun setLoggedIn(isLoggedIn: Boolean) {
        editor.putBoolean(IS_LOGGED_IN, isLoggedIn)
        editor.commit()
    }
    
    /**
     * Check if user is logged in
     */
    fun isLoggedIn(): Boolean {
        return sharedPreferences.getBoolean(IS_LOGGED_IN, false)
    }
    
    /**
     * Save user details
     */
    fun saveUserDetails(name: String, email: String) {
        editor.putString(USER_NAME, name)
        editor.putString(USER_EMAIL, email)
        editor.commit()
    }
    
    /**
     * Save user credentials (email and password)
     */
    fun saveUserCredentials(email: String, password: String) {
        editor.putString(USER_EMAIL, email)
        editor.putString(USER_PASSWORD, hashPassword(password))
        editor.commit()
    }
    
    /**
     * Check if user exists
     */
    fun userExists(email: String): Boolean {
        val storedEmail = sharedPreferences.getString(USER_EMAIL, null)
        return storedEmail == email
    }
    
    /**
     * Validate user credentials
     */
    fun validateCredentials(email: String, password: String): Boolean {
        val storedEmail = sharedPreferences.getString(USER_EMAIL, null)
        val storedPassword = sharedPreferences.getString(USER_PASSWORD, null)
        
        return storedEmail == email && storedPassword == hashPassword(password)
    }
    
    /**
     * Hash password for basic security
     */
    private fun hashPassword(password: String): String {
        return try {
            val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
            bytes.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            // Fallback if hashing fails
            password
        }
    }
    
    /**
     * Get user name
     */
    fun getUserName(): String {
        return sharedPreferences.getString(USER_NAME, "") ?: ""
    }
    
    /**
     * Get user email
     */
    fun getUserEmail(): String {
        return sharedPreferences.getString(USER_EMAIL, "") ?: ""
    }
    
    /**
     * Set monthly budget
     */
    fun setMonthlyBudget(budget: Float) {
        editor.putFloat(MONTHLY_BUDGET, budget)
        editor.commit()
    }
    
    /**
     * Get monthly budget
     */
    fun getMonthlyBudget(): Float {
        return sharedPreferences.getFloat(MONTHLY_BUDGET, 0f)
    }
    
    /**
     * Set dark mode preference
     */
    fun setDarkMode(enabled: Boolean) {
        editor.putBoolean(DARK_MODE, enabled)
        editor.commit()
    }
    
    /**
     * Get dark mode preference
     */
    fun isDarkModeEnabled(): Boolean {
        return sharedPreferences.getBoolean(DARK_MODE, false)
    }
    
    /**
     * Set notifications preference
     */
    fun setNotificationsEnabled(enabled: Boolean) {
        editor.putBoolean(NOTIFICATIONS_ENABLED, enabled)
        editor.commit()
    }
    
    /**
     * Get notifications preference
     */
    fun areNotificationsEnabled(): Boolean {
        return sharedPreferences.getBoolean(NOTIFICATIONS_ENABLED, true)
    }
    
    /**
     * Set daily reminder preference
     */
    fun setDailyReminderEnabled(enabled: Boolean) {
        editor.putBoolean(DAILY_REMINDER_ENABLED, enabled)
        editor.commit()
    }
    
    /**
     * Get daily reminder preference
     */
    fun isDailyReminderEnabled(): Boolean {
        return sharedPreferences.getBoolean(DAILY_REMINDER_ENABLED, true)
    }
    
    /**
     * Set reminder time
     */
    fun setReminderTime(hour: Int, minute: Int) {
        editor.putInt(REMINDER_HOUR, hour)
        editor.putInt(REMINDER_MINUTE, minute)
        editor.commit()
    }
    
    /**
     * Get reminder hour
     */
    fun getReminderHour(): Int {
        return sharedPreferences.getInt(REMINDER_HOUR, 20) // Default to 8 PM
    }
    
    /**
     * Get reminder minute
     */
    fun getReminderMinute(): Int {
        return sharedPreferences.getInt(REMINDER_MINUTE, 0) // Default to 0 minutes
    }
    
    /**
     * Clear all preferences (used for logout)
     */
    fun clearPreferences() {
        // Keep first time launch preference
        val isFirstTimeLaunch = isFirstTimeLaunch()
        editor.clear()
        editor.putBoolean(IS_FIRST_TIME_LAUNCH, false) // Never show onboarding again after login
        editor.commit()
    }
} 