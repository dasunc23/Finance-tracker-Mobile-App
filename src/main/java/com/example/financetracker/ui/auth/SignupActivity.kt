package com.example.financetracker.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.financetracker.databinding.ActivitySignupBinding
import com.example.financetracker.ui.main.MainActivity

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        viewModel = ViewModelProvider(this)[AuthViewModel::class.java]
        
        setupListeners()
        observeViewModel()
    }
    
    private fun setupListeners() {
        // Sign up button
        binding.btnSignup.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()
            
            // Validation is done in the ViewModel
            viewModel.signup(this, name, email, password, confirmPassword)
        }
        
        // Already have account - go to login
        binding.tvLogin.setOnClickListener {
            finish() // Go back to login activity
        }
    }
    
    private fun observeViewModel() {
        // Observe signup result
        viewModel.signupResult.observe(this, Observer { success ->
            if (success) {
                Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show()
                navigateToMainActivity()
            }
        })
        
        // Observe loading state
        viewModel.isLoading.observe(this, Observer { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnSignup.isEnabled = !isLoading
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
} 