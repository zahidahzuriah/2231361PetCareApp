package com.example.j104867petcareapp

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.icu.util.Calendar
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.j104867petcareapp.databinding.ActivityAdminDashboardBinding
import com.example.j104867petcareapp.models.Booking
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminDashboardBinding
    private lateinit var databaseBookings: DatabaseReference
    private lateinit var bookingList: ArrayList<Booking>
    private lateinit var bookingAdapter: AdminBookingAdapter
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable the Up button for navigating back to the previous screen
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Initialize View Binding
        binding = ActivityAdminDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase references
        databaseBookings = FirebaseDatabase.getInstance().reference.child("Bookings")
        auth = FirebaseAuth.getInstance()

        // Set up click listeners for managing services and appointments
        binding.viewServicesButton.setOnClickListener {
            startActivity(Intent(this, ViewServicesActivity::class.java))
        }

        binding.manageAppointmentsButton.setOnClickListener {
            startActivity(Intent(this, AdminManageAppointmentActivity::class.java))
        }

        binding.logoutCard.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        // Initialize booking list and adapter
        bookingList = arrayListOf()
        bookingAdapter = AdminBookingAdapter(
            bookings = bookingList,
            onAcceptClick = { booking -> updateBookingStatus(booking, "Accepted") },
            onPendingClick = { booking -> updateBookingStatus(booking, "Pending") },
            onEditClick = { booking -> editBookingDateAndTime(booking) },
            onDeleteClick = { booking -> deleteBooking(booking) }
        )

        // Fetch bookings for management
        fetchBookings()
    }

    private fun fetchBookings() {
        databaseBookings.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                bookingList.clear()
                for (bookingSnapshot in snapshot.children) {
                    val booking = bookingSnapshot.getValue(Booking::class.java)
                    if (booking != null) {
                        bookingList.add(booking)
                    }
                }
                bookingAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@AdminDashboardActivity, "Failed to load bookings", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateBookingStatus(booking: Booking, status: String) {
        databaseBookings.child(booking.bookingId).child("status").setValue(status)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Toast.makeText(this, "Booking status updated to $status", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to update booking status", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun editBookingDateAndTime(booking: Booking) {
        val calendar = android.icu.util.Calendar.getInstance()

        DatePickerDialog(this, { _, year, month, dayOfMonth ->
            val selectedDate = "$year-${month + 1}-$dayOfMonth"

            TimePickerDialog(this, { _, hour, minute ->
                val selectedTime = String.format("%02d:%02d", hour, minute)
                updateBookingDateAndTime(booking, selectedDate, selectedTime)
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()

        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun updateBookingDateAndTime(booking: Booking, newDate: String, newTime: String) {
        val updatedBooking = booking.copy(bookingDate = newDate, bookingTime = newTime)
        databaseBookings.child(booking.bookingId).setValue(updatedBooking)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Toast.makeText(this, "Booking date and time updated", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to update booking", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun deleteBooking(booking: Booking) {
        databaseBookings.child(booking.bookingId).removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Booking deleted successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Failed to delete booking", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Handle menu item clicks (e.g., logout)
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
