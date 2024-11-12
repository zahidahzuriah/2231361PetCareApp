package com.example.j104867petcareapp

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.j104867petcareapp.databinding.ActivityPaymentSuccessBinding
import com.google.firebase.auth.FirebaseAuth

class PaymentSuccessActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPaymentSuccessBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable the Up button for navigating back to the previous screen
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Initialize View Binding
        binding = ActivityPaymentSuccessBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Retrieve payment details from intent
        val receiptId = intent.getStringExtra("receiptId") ?: "Unknown Receipt"
        val serviceName = intent.getStringExtra("serviceName") ?: "Service"
        val servicePrice = intent.getDoubleExtra("servicePrice", 0.0)
        val paymentMethod = intent.getStringExtra("paymentMethod") ?: "N/A"
        val petName = intent.getStringExtra("petName") ?: "Unknown Pet"
        val petOwnerName = intent.getStringExtra("petOwnerName") ?: "Unknown Owner"

        // Set success message
        binding.successMessageTextView.text = "Your payment was successful!"

        // Set receipt details
        binding.receiptIdTextView.text = "Receipt ID: $receiptId"
        binding.receiptServiceNameTextView.text = "Service: $serviceName"
        binding.receiptServicePriceTextView.text = String.format("Price: %.2f", servicePrice)
        binding.receiptPaymentMethodTextView.text = "Payment Method: $paymentMethod"
        binding.receiptPetNameTextView.text = "Pet Name: $petName"
        binding.receiptPetOwnerNameTextView.text = "Pet Owner: $petOwnerName"

        // Set up button to navigate back to the user dashboard or another activity
        binding.backToDashboardButton.setOnClickListener {
            val intent = Intent(this, UserDashboardActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }
    // Handle menu item clicks
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                // Log out the user and go to Login Activity
                auth.signOut()
                Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
