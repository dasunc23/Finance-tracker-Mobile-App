package com.example.financialtracker

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.financetracker.ui.splash.SplashActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Redirect to the actual app's splash screen
        startActivity(Intent(this, SplashActivity::class.java))
        finish()
    }
}