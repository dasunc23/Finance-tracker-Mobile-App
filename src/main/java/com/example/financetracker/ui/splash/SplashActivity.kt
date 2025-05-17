package com.example.financetracker.ui.splash

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.example.financetracker.R
import com.example.financetracker.databinding.ActivitySplashBinding
import com.example.financetracker.ui.onboarding.OnboardingActivity
import com.example.financetracker.util.PreferenceHelper

class SplashActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivitySplashBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Setup button click listener - ONLY way to proceed
        binding.btnGetStarted.setOnClickListener {
            // Go to onboarding screens
            val intent = Intent(this, OnboardingActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
} 