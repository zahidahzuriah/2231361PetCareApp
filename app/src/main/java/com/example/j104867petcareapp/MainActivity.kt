package com.example.j104867petcareapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.j104867petcareapp.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

            // Initialize View Binding
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)

            // Initialize Firebase
            auth = FirebaseAuth.getInstance()
            database = FirebaseDatabase.getInstance().reference

            binding.loginButton.setOnClickListener {
                val email = binding.emailEditText.text.toString().trim()
                val password = binding.passwordEditText.text.toString().trim()

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Authenticate the user
                auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val userId = auth.currentUser?.uid ?: return@addOnCompleteListener
                        // Fetch the user's role
                        database.child("Users").child(userId).child("role").get().addOnSuccessListener {
                            val role = it.value.toString()
                            if (role == "Admin") {
                                startActivity(Intent(this, AdminDashboardActivity::class.java))
                            } else {
                                startActivity(Intent(this, UserDashboardActivity::class.java))
                            }
                            finish()
                        }.addOnFailureListener {
                            Toast.makeText(this, "Failed to fetch role", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this, "Login failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            // Register link to open RegisterActivity
            binding.registerLinkTextView.setOnClickListener {
                val intent = Intent(this, RegisterActivity::class.java)
                startActivity(intent)
            }
        }
    }