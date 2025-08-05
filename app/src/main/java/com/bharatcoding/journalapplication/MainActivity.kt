package com.bharatcoding.journalapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.bharatcoding.journalapplication.databinding.ActivityMainBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
        
        // Initialize Firebase Auth
        auth = Firebase.auth

        // Set up click listeners
        binding.loginButton.setOnClickListener {
            signInUser()
        }

        binding.createAccountButton.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
    }

    private fun signInUser() {
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

        // Clear previous errors
        binding.emailLayout.error = null
        binding.passwordLayout.error = null

        // Show loading state
        binding.loginButton.isEnabled = false
        binding.loginButton.text = "Signing in..."

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("TAG", "signInWithEmail:success")
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("TAG", "signInWithEmail:failure", task.exception)
                    val errorMessage = when {
                        task.exception?.message?.contains("no user record") == true -> 
                            "No account found with this email. Please create an account."
                        task.exception?.message?.contains("password is invalid") == true -> 
                            "Incorrect password. Please try again."
                        task.exception?.message?.contains("network") == true -> 
                            "Network error. Please check your internet connection."
                        else -> "Authentication failed. Please try again."
                    }
                    Toast.makeText(baseContext, errorMessage, Toast.LENGTH_LONG).show()
                    updateUI(null)
                }
            }
    }

    private fun updateUI(user: FirebaseUser?) {
        // Reset button state
        binding.loginButton.isEnabled = true
        binding.loginButton.text = "Login"

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
            // User is already signed in, navigate to JournalList
            val intent = Intent(this, JournalList::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}