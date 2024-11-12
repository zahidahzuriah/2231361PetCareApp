package com.example.j104867petcareapp

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.j104867petcareapp.databinding.ActivityPaymentBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class PaymentActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPaymentBinding
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable the Up button for navigating back to the previous screen
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Initialize View Binding
        binding = ActivityPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()
        userId = auth.currentUser?.uid ?: ""

        val serviceName = intent.getStringExtra("serviceName") ?: "Service"
        val servicePrice = intent.getDoubleExtra("servicePrice", 0.0)
        val petName = intent.getStringExtra("petName") ?: "Unknown Pet"
        val petOwnerName = intent.getStringExtra("petOwnerName") ?: "Unknown Owner"

        // Initialize Firebase Database reference for payments
        database = FirebaseDatabase.getInstance().reference.child("Payments").child(userId)

        // Display service name and price
        binding.serviceNameTextView.text = serviceName
        binding.servicePriceTextView.text = String.format("Price: %.2f", servicePrice)

        // Set up payment options
        val paymentOptions = listOf("Online Payment", "Cash Payment")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, paymentOptions)
        binding.paymentOptionsSpinner.adapter = adapter

        // Show card details form only if "Online Payment" is selected
        binding.paymentOptionsSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position == 0) {
                    binding.cardDetailsLayout.visibility = View.VISIBLE
                } else {
                    binding.cardDetailsLayout.visibility = View.GONE
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }

        // Set up Confirm Payment button click listener
        binding.confirmPaymentButton.setOnClickListener {
            if (validatePaymentForm()) {
                handlePayment(serviceName, servicePrice, petName, petOwnerName)
            }
        }
    }

    private fun validatePaymentForm(): Boolean {
        val selectedPaymentOption = binding.paymentOptionsSpinner.selectedItem.toString()
        if (selectedPaymentOption == "Online Payment") {
            val cardNumber = binding.cardNumberEditText.text.toString().trim()
            val cardExpiry = binding.cardExpiryEditText.text.toString().trim()
            val cardCVV = binding.cardCVVEditText.text.toString().trim()

            if (cardNumber.isEmpty() || cardExpiry.isEmpty() || cardCVV.isEmpty()) {
                showToast("Please fill in all card details.")
                return false
            }

            if (!isCardNumberValid(cardNumber)) {
                showToast("Invalid card number.")
                return false
            }

            if (!isCardExpiryValid(cardExpiry)) {
                showToast("Invalid expiry date format. Use MM/YY.")
                return false
            }

            if (cardCVV.length != 3) {
                showToast("Invalid CVV.")
                return false
            }
        }
        return true
    }

    private fun handlePayment(serviceName: String, servicePrice: Double, petName: String, petOwnerName: String) {
        val selectedPaymentOption = binding.paymentOptionsSpinner.selectedItem.toString()
        val paymentId = database.push().key ?: return

        if (selectedPaymentOption == "Online Payment") {
            val cardNumber = binding.cardNumberEditText.text.toString().trim()
            val cardExpiry = binding.cardExpiryEditText.text.toString().trim()

            // Encrypt card details before saving (for demonstration, using plain text)
            val encryptedCardNumber = encrypt(cardNumber) // Placeholder function
            val encryptedCardExpiry = encrypt(cardExpiry) // Placeholder function

            val payment = mapOf(
                "paymentId" to paymentId,
                "serviceName" to serviceName,
                "servicePrice" to servicePrice,
                "paymentMethod" to "Online Payment",
                "cardNumber" to encryptedCardNumber,
                "cardExpiry" to encryptedCardExpiry,
                "status" to "Processing",
                "petName" to petName,
                "petOwnerName" to petOwnerName
            )
            database.child(paymentId).setValue(payment).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    showToast("Online payment recorded successfully.")
                    navigateToSuccessPage(paymentId, serviceName, servicePrice, selectedPaymentOption, petName, petOwnerName)
                } else {
                    showToast("Failed to record payment.")
                }
            }
        } else if (selectedPaymentOption == "Cash Payment") {
            val payment = mapOf(
                "paymentId" to paymentId,
                "serviceName" to serviceName,
                "servicePrice" to servicePrice,
                "paymentMethod" to "Cash Payment",
                "status" to "Pending",
                "petName" to petName,
                "petOwnerName" to petOwnerName
            )
            database.child(paymentId).setValue(payment).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    showToast("Cash payment recorded successfully. Please pay at the service center.")
                    navigateToSuccessPage(paymentId, serviceName, servicePrice, selectedPaymentOption, petName, petOwnerName)
                } else {
                    showToast("Failed to record payment.")
                }
            }
        }
    }
    private fun navigateToSuccessPage(
        paymentId: String,
        serviceName: String,
        servicePrice: Double,
        paymentMethod: String,
        petName: String,
        petOwnerName: String
    ) {
        val intent = Intent(this, PaymentSuccessActivity::class.java).apply {
            putExtra("receiptId", paymentId)
            putExtra("serviceName", serviceName)
            putExtra("servicePrice", servicePrice)
            putExtra("paymentMethod", paymentMethod)
            putExtra("petName", petName)
            putExtra("petOwnerName", petOwnerName)
        }
        startActivity(intent)
    }


    private fun isCardNumberValid(cardNumber: String): Boolean {
        return cardNumber.length == 16
    }

    private fun isCardExpiryValid(cardExpiry: String): Boolean {
        val regex = Regex("(0[1-9]|1[0-2])/\\d{2}")
        return cardExpiry.matches(regex)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
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

// Placeholder encryption function
    private fun encrypt(data: String): String {
        // Implement encryption logic here
        return data.reversed() // Dummy encryption for demonstration purposes
    }
