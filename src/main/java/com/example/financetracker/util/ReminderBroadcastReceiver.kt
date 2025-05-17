package com.example.financetracker.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * BroadcastReceiver for handling daily reminder alarms
 */
class ReminderBroadcastReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        val preferenceHelper = PreferenceHelper(context)
        
        // Only show notification if enabled in preferences
        if (preferenceHelper.areNotificationsEnabled()) {
            val notificationHelper = NotificationHelper(context)
            notificationHelper.showDailyReminderNotification()
        }
    }
} 