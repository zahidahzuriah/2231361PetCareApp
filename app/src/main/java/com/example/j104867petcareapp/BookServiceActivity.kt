package com.example.j104867petcareapp

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.icu.util.Calendar
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.j104867petcareapp.databinding.ActivityBookServiceBinding
import com.example.j104867petcareapp.models.Booking
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class BookServiceActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBookServiceBinding
    private lateinit var database: DatabaseReference
    private lateinit var serviceId: String
    private lateinit var serviceName: String
    private var servicePrice: Double = 0.0
    private lateinit var serviceDescription: String
    private lateinit var auth: FirebaseAuth
    private var userEmail: String = ""
    private var userId: String = ""
    private var petOwnerName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable the Up button for navigating back to the previous screen
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Initialize View Binding
        binding = ActivityBookServiceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Retrieve current user's ID and email
        val currentUser = auth.currentUser
        if (currentUser != null) {
            userId = currentUser.uid
            userEmail = currentUser.email ?: ""
        }

        // Retrieve service details from intent extras
        serviceId = intent.getStringExtra("serviceId") ?: return
        serviceName = intent.getStringExtra("serviceName") ?: "Service"
        servicePrice = intent.getDoubleExtra("servicePrice", 0.0)
        serviceDescription = intent.getStringExtra("serviceDescription") ?: "Description not available"

        // Display the selected service name, price, and description
        binding.serviceNameTextView.text = serviceName
        binding.servicePriceTextView.text = String.format("Price: %.2f", servicePrice)
        binding.serviceDescriptionTextView.text = serviceDescription

        // Initialize Firebase Database reference for bookings
        database = FirebaseDatabase.getInstance().reference.child("Bookings")

        // Retrieve user's name from Firebase and set it as the pet owner name
        fetchUserName()

        // Set up date and time pickers for booking
        setupDateAndTimePickers()

        // Set up Book Service button click listener
        binding.bookServiceButton.setOnClickListener {
            if (validateBookingForm()) {
                checkAvailabilityAndProceed()
            } else {
                showToast("Please fill in all fields before proceeding to payment.")
            }
        }
    }

    private fun fetchUserName() {
        val userRef = FirebaseDatabase.getInstance().reference.child("Users").child(userId)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                petOwnerName = snapshot.child("username").getValue(String::class.java) ?: "Unknown Owner"
                binding.petOwnerNameTextView.text = petOwnerName // Set the retrieved name to the TextView
            }

            override fun onCancelled(error: DatabaseError) {
                showToast("Failed to retrieve user information.")
            }
        })
    }

    private fun setupDateAndTimePickers() {
        // Set up DatePickerDialog for booking date
        binding.bookingDateTextView.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                val date = "$selectedYear-${selectedMonth + 1}-$selectedDay"
                binding.bookingDateTextView.text = date
            }, year, month, day)

            datePickerDialog.show()
        }

        // Set up TimePickerDialog for booking time
        binding.bookingTimeTextView.setOnClickListener {
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            val timePickerDialog = TimePickerDialog(this, { _, selectedHour, selectedMinute ->
                if (selectedHour in 8..16) {
                    val time = String.format("%02d:%02d", selectedHour, selectedMinute)
                    binding.bookingTimeTextView.text = time
                } else {
                    showToast("Please select a time between 08:00 and 17:00")
                }
            }, hour, minute, true)

            // Set time picker to start from 8:00 AM to 5:00 PM
            timePickerDialog.updateTime(8, 0) // Default to 8:00 AM when the picker opens
            timePickerDialog.show()
        }
    }

    private fun validateBookingForm(): Boolean {
        val bookingDate = binding.bookingDateTextView.text.toString().trim()
        val bookingTime = binding.bookingTimeTextView.text.toString().trim()
        val petType = binding.petTypeEditText.text.toString().trim()
        val petName = binding.petNameEditText.text.toString().trim()

        return bookingDate.isNotEmpty() && bookingTime.isNotEmpty() && petType.isNotEmpty() && petName.isNotEmpty()
    }

    private fun checkAvailabilityAndProceed() {
        val bookingDate = binding.bookingDateTextView.text.toString().trim()
        val bookingTime = binding.bookingTimeTextView.text.toString().trim()
        val bookingDateTime = "$bookingDate $bookingTime"

        // Check if the selected date and time is available
        database.orderByChild("bookingDateTime").equalTo(bookingDateTime)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        showToast("The selected date and time is already booked. Please choose a different time.")
                    } else {
                        handleBooking(bookingDateTime)
                        openPaymentPage()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    showToast("Failed to check availability. Please try again.")
                }
            })
    }

    private fun handleBooking(bookingDateTime: String) {
        val bookingDate = binding.bookingDateTextView.text.toString().trim()
        val bookingTime = binding.bookingTimeTextView.text.toString().trim()
        val petType = binding.petTypeEditText.text.toString().trim()
        val petName = binding.petNameEditText.text.toString().trim()

        // Create booking entry in Firebase
        val bookingId = database.push().key ?: return
        val booking = Booking(
            bookingId = bookingId,
            serviceId = serviceId,
            serviceName = serviceName,
            bookingDate = bookingDate,
            bookingTime = bookingTime,
            petName = petName,
            petOwnerName = petOwnerName,
            userId = userId,
            status = "Pending",
            bookingDateTime = bookingDateTime // Store the combined booking date and time
        )

        database.child(bookingId).setValue(booking).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                showToast("Booking successful")
            } else {
                showToast("Failed to book service")
            }
        }
    }

    private fun openPaymentPage() {
        val intent = Intent(this, PaymentActivity::class.java)
        intent.putExtra("serviceName", serviceName)
        intent.putExtra("servicePrice", servicePrice)
        intent.putExtra("serviceDescription", serviceDescription)
        intent.putExtra("petName", binding.petNameEditText.text.toString().trim())
        intent.putExtra("petOwnerName", petOwnerName)
        intent.putExtra("petType", binding.petTypeEditText.text.toString().trim())
        startActivity(intent)
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
