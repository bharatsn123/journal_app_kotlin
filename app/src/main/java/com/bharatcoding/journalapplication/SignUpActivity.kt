package com.bharatcoding.journalapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.bharatcoding.journalapplication.databinding.ActivitySignUpBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth

class SignUpActivity : AppCompatActivity() {
    lateinit var binding: ActivitySignUpBinding
    private lateinit var auth: FirebaseAuth
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sign_up)
        // Initialize Firebase Auth
        auth = Firebase.auth

        binding.signUpButton.setOnClickListener() {
            createUser()
        }
    }

    private fun createUser() {
        val email = binding.emailInput.text.toString().trim()
        val password = binding.passwordInput.text.toString().trim()

        // Validate input
        if (email.isEmpty()) {
            binding.emailLayout.error = "Email is required"
            return
        }

        if (password.isEmpty()) {
            binding.passwordLayout.error = "Password is required"
            return
        }

        if (password.length < 6) {
            binding.passwordLayout.error = "Password must be at least 6 characters"
            return
        }

        // Clear previous errors
        binding.emailLayout.error = null
        binding.passwordLayout.error = null

        // Show loading state
        binding.signUpButton.isEnabled = false
        binding.signUpButton.text = "Creating Account..."

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("TAG", "createUserWithEmail:success")
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("TAG", "createUserWithEmail:failure", task.exception)
                    val errorMessage = when {
                        task.exception?.message?.contains("email address is already in use") == true -> 
                            "An account with this email already exists. Please sign in instead."
                        task.exception?.message?.contains("badly formatted") == true -> 
                            "Please enter a valid email address."
                        task.exception?.message?.contains("network") == true -> 
                            "Network error. Please check your internet connection."
                        else -> "Account creation failed. Please try again."
                    }
                    Toast.makeText(baseContext, errorMessage, Toast.LENGTH_LONG).show()
                    updateUI(null)
                }
            }
    }

    private fun updateUI(user: FirebaseUser?) {
        // Reset button state
        binding.signUpButton.isEnabled = true
        binding.signUpButton.text = "Create Account"

        if (user != null) {
            // User is signed in, navigate to JournalList
            val intent = Intent(this, JournalList::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if (currentUser != null) {
            reload()
        }
    }

    public fun reload() {
        // User is already signed in, navigate to JournalList
        val intent = Intent(this, JournalList::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}