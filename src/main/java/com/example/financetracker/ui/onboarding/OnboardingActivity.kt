package com.example.financetracker.ui.onboarding

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.viewpager2.widget.ViewPager2
import com.example.financetracker.R
import com.example.financetracker.databinding.ActivityOnboardingBinding
import com.example.financetracker.ui.auth.LoginActivity
import com.example.financetracker.util.PreferenceHelper

class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding
    private lateinit var adapter: OnboardingAdapter
    private lateinit var preferenceHelper: PreferenceHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        preferenceHelper = PreferenceHelper(this)
        adapter = OnboardingAdapter(this)
        
        setupViewPager()
        setupListeners()
    }
    
    private fun setupViewPager() {
        binding.viewPager.adapter = adapter
        binding.dotsIndicator.attachTo(binding.viewPager)
        
        // Disable swiping to ensure users click next buttons
        binding.viewPager.isUserInputEnabled = false
        
        // Initial page setup
        updateButtonsForPage(0)
        
        // Page change callback
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateButtonsForPage(position)
            }
        })
    }
    
    private fun updateButtonsForPage(position: Int) {
        when (position) {
            0 -> {
                binding.btnNext.text = getString(R.string.next)
                binding.tvSkip.visibility = View.VISIBLE
            }
            1 -> {
                binding.btnNext.text = getString(R.string.next)
                binding.tvSkip.visibility = View.VISIBLE
            }
            2 -> {
                binding.btnNext.text = getString(R.string.get_started)
                binding.tvSkip.visibility = View.GONE
            }
        }
    }
    
    private fun setupListeners() {
        // Next button click
        binding.btnNext.setOnClickListener {
            val currentPosition = binding.viewPager.currentItem
            
            if (currentPosition < adapter.itemCount - 1) {
                // Go to next onboarding page
                binding.viewPager.currentItem = currentPosition + 1
            } else {
                // On last page, go to login
                goToLoginScreen()
            }
        }
        
        // Skip button click
        binding.tvSkip.setOnClickListener {
            goToLoginScreen()
        }
    }
    
    private fun goToLoginScreen() {
        // Mark onboarding as completed
        preferenceHelper.setFirstTimeLaunch(false)
        
        // Navigate to login
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
    
    // Disable back button to ensure users go through the proper flow
    override fun onBackPressed() {
        if (binding.viewPager.currentItem > 0) {
            // Go back to previous page if not on first page
            binding.viewPager.currentItem = binding.viewPager.currentItem - 1
        } else {
            // Otherwise do nothing - don't allow going back to splash
            // super.onBackPressed() // Commented out to prevent going back
        }
    }
} 