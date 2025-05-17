package com.example.financetracker.ui.auth

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.financetracker.R
import com.example.financetracker.databinding.ActivityForgotPasswordBinding

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding
    private lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        viewModel = ViewModelProvider(this)[AuthViewModel::class.java]
        
        // Set title
        setTitle(R.string.reset_password)
        
        setupClickListeners()
        observeViewModel()
    }
    
    private fun setupClickListeners() {
        // Back button
        binding.ivBack.setOnClickListener {
            onBackPressed()
        }
        
        // Reset password button
        binding.btnResetPassword.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            // Call ViewModel to handle password reset
            viewModel.resetPassword(this, email)
        }
    }
    
    private fun observeViewModel() {
        // Observe reset password result
        viewModel.resetPasswordResult.observe(this, Observer { success ->
            if (success) {
                Toast.makeText(this, "Password reset instructions sent to your email", Toast.LENGTH_LONG).show()
                finish()
            }
        })
        
        // Observe loading state
        viewModel.isLoading.observe(this, Observer { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnResetPassword.isEnabled = !isLoading
        })
        
        // Observe error messages
        viewModel.errorMessage.observe(this, Observer { message ->
            if (!message.isNullOrEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }
        })
    }
} 