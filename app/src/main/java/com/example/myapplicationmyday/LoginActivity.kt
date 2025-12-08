package com.example.myapplicationmyday

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplicationmyday.data.User
import com.example.myapplicationmyday.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth and Firestore
        auth = Firebase.auth
        firestore = Firebase.firestore

        // Check if user is already signed in
        if (auth.currentUser != null) {
            navigateToHome()
            return
        }

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnSignIn.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (validateInput(email, password)) {
                signIn(email, password)
            }
        }

        binding.btnSignUp.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (validateInput(email, password)) {
                signUp(email, password)
            }
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        if (email.isEmpty()) {
            binding.tilEmail.error = getString(R.string.error_email_required)
            return false
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = getString(R.string.error_invalid_email)
            return false
        }

        binding.tilEmail.error = null

        if (password.isEmpty()) {
            binding.tilPassword.error = getString(R.string.error_password_required)
            return false
        }

        if (password.length < 6) {
            binding.tilPassword.error = getString(R.string.error_password_too_short)
            return false
        }

        binding.tilPassword.error = null
        return true
    }

    private fun signIn(email: String, password: String) {
        showLoading(true)

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                showLoading(false)
                if (task.isSuccessful) {
                    // Sign in success
                    Toast.makeText(
                        this,
                        getString(R.string.sign_in_success),
                        Toast.LENGTH_SHORT
                    ).show()
                    navigateToHome()
                } else {
                    // Sign in failed
                    Toast.makeText(
                        this,
                        getString(R.string.sign_in_failed, task.exception?.message),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun signUp(email: String, password: String) {
        showLoading(true)

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign up success - Create user profile in Firestore
                    createUserProfile(email)
                } else {
                    // Sign up failed
                    showLoading(false)
                    Toast.makeText(
                        this,
                        getString(R.string.sign_up_failed, task.exception?.message),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }
    
    private fun createUserProfile(email: String) {
        val userId = auth.currentUser?.uid ?: return
        
        val newUser = User(
            uid = userId,
            email = email,
            displayName = "",
            username = "",
            photoUrl = "",
            bio = ""
        )
        
        firestore.collection("users").document(userId)
            .set(newUser)
            .addOnSuccessListener {
                showLoading(false)
                Toast.makeText(
                    this,
                    getString(R.string.sign_up_success),
                    Toast.LENGTH_SHORT
                ).show()
                navigateToHome()
            }
            .addOnFailureListener {
                showLoading(false)
                Toast.makeText(
                    this,
                    "Error al crear perfil: ${it.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnSignIn.isEnabled = !isLoading
        binding.btnSignUp.isEnabled = !isLoading
        binding.etEmail.isEnabled = !isLoading
        binding.etPassword.isEnabled = !isLoading
    }

    private fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
