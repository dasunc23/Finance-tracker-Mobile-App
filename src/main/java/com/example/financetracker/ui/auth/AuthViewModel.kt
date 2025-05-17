package com.example.financetracker.ui.auth

import android.content.Context
import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.financetracker.util.PreferenceHelper
import java.util.concurrent.Executors

class AuthViewModel : ViewModel() {
    
    private val _loginResult = MutableLiveData<Boolean>()
    val loginResult: LiveData<Boolean> = _loginResult
    
    private val _signupResult = MutableLiveData<Boolean>()
    val signupResult: LiveData<Boolean> = _signupResult
    
    private val _resetPasswordResult = MutableLiveData<Boolean>()
    val resetPasswordResult: LiveData<Boolean> = _resetPasswordResult
    
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val executor = Executors.newSingleThreadExecutor()
    
    /**
     * Attempt to login user with email and password
     */
    fun login(context: Context, email: String, password: String) {
        // Validate inputs
        if (!validateLoginInputs(email, password)) return
        
        _isLoading.value = true
        
        executor.execute {
            val preferenceHelper = PreferenceHelper(context)
            
            // Check if the user exists 
            if (!preferenceHelper.userExists(email)) {
                _errorMessage.postValue("No account found with this email")
                _loginResult.postValue(false)
                _isLoading.postValue(false)
                return@execute
            }
            
            // Validate credentials
            if (preferenceHelper.validateCredentials(email, password)) {
                preferenceHelper.setLoggedIn(true)
                _loginResult.postValue(true)
            } else {
                _errorMessage.postValue("Invalid password")
                _loginResult.postValue(false)
            }
            
            _isLoading.postValue(false)
        }
    }
    
    /**
     * Register a new user
     */
    fun signup(context: Context, name: String, email: String, password: String, confirmPassword: String) {
        // Validate inputs
        if (!validateSignupInputs(name, email, password, confirmPassword)) return
        
        _isLoading.value = true
        
        executor.execute {
            val preferenceHelper = PreferenceHelper(context)
            
            // Check if the user already exists
            if (preferenceHelper.userExists(email)) {
                _errorMessage.postValue("An account with this email already exists")
                _signupResult.postValue(false)
                _isLoading.postValue(false)
                return@execute
            }
            
            // Save the user details and credentials
            preferenceHelper.saveUserDetails(name, email)
            preferenceHelper.saveUserCredentials(email, password)
            preferenceHelper.setLoggedIn(true)
            
            _signupResult.postValue(true)
            _isLoading.postValue(false)
        }
    }
    
    /**
     * Send password reset request
     */
    fun resetPassword(context: Context, email: String) {
        // Validate email
        if (!validateEmail(email)) {
            _errorMessage.value = "Please enter a valid email address"
            return
        }
        
        _isLoading.value = true
        
        executor.execute {
            val preferenceHelper = PreferenceHelper(context)
            
            // Check if the user exists
            if (!preferenceHelper.userExists(email)) {
                _errorMessage.postValue("No account found with this email")
                _resetPasswordResult.postValue(false)
                _isLoading.postValue(false)
                return@execute
            }
            
            // In a real app, this would send a password reset email
            // For this demo, we'll just simulate success
            _resetPasswordResult.postValue(true)
            _isLoading.postValue(false)
        }
    }
    
    /**
     * Validate login form inputs
     */
    private fun validateLoginInputs(email: String, password: String): Boolean {
        if (email.isEmpty() || password.isEmpty()) {
            _errorMessage.value = "Please fill in all fields"
            return false
        }
        
        if (!validateEmail(email)) {
            _errorMessage.value = "Please enter a valid email address"
            return false
        }
        
        return true
    }
    
    /**
     * Validate signup form inputs
     */
    private fun validateSignupInputs(name: String, email: String, password: String, confirmPassword: String): Boolean {
        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            _errorMessage.value = "Please fill in all fields"
            return false
        }
        
        if (!validateEmail(email)) {
            _errorMessage.value = "Please enter a valid email address"
            return false
        }
        
        if (password.length < 6) {
            _errorMessage.value = "Password must be at least 6 characters"
            return false
        }
        
        if (password != confirmPassword) {
            _errorMessage.value = "Passwords do not match"
            return false
        }
        
        return true
    }
    
    /**
     * Validate email format
     */
    private fun validateEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    
    override fun onCleared() {
        super.onCleared()
        executor.shutdown()
    }
} 