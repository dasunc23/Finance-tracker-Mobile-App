package com.example.financetracker.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.financetracker.R
import com.example.financetracker.databinding.ActivityLoginBinding
import com.example.financetracker.ui.main.MainActivity
import com.example.financetracker.util.PreferenceHelper

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: AuthViewModel
    private lateinit var preferenceHelper: PreferenceHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        viewModel = ViewModelProvider(this)[AuthViewModel::class.java]
        preferenceHelper = PreferenceHelper(this)
        
        // If already logged in, go to main activity
        if (preferenceHelper.isLoggedIn()) {
            navigateToMainActivity()
            return
        }
        
        setupClickListeners()
        observeViewModel()
    }
    
    private fun setupClickListeners() {
        // Login button
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            
            // Login using ViewModel
            viewModel.login(this, email, password)
        }
        
        // Signup text
        binding.tvSignUp.setOnClickListener {
            // Navigate to SignupActivity
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }
        
        // Forgot password
        binding.tvForgotPassword.setOnClickListener {
            val intent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }
    }
    
    private fun observeViewModel() {
        // Observe login result
        viewModel.loginResult.observe(this, Observer { success ->
            if (success) {
                navigateToMainActivity()
            }
        })
        
        // Observe loading state
        viewModel.isLoading.observe(this, Observer { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnLogin.isEnabled = !isLoading
        })
        
        // Observe error messages
        viewModel.errorMessage.observe(this, Observer { message ->
            if (!message.isNullOrEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }
        })
    }
    
    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
    
    // Disable back button to prevent going back to onboarding
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Do nothing to prevent going back
        // super.onBackPressed() // Commented out intentionally
    }
} 