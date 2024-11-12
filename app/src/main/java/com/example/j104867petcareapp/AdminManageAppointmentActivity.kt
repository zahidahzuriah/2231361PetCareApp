package com.example.j104867petcareapp

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.j104867petcareapp.databinding.ActivityAdminManageAppointmentBinding
import com.example.j104867petcareapp.models.Booking
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Calendar

class AdminManageAppointmentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminManageAppointmentBinding
    private lateinit var databaseBookings: DatabaseReference
    private lateinit var bookingList: ArrayList<Booking>
    private lateinit var bookingAdapter: AdminBookingAdapter
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable the Up button for navigating back to the previous screen
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Initialize View Binding
        binding = ActivityAdminManageAppointmentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        databaseBookings = FirebaseDatabase.getInstance().reference.child("Bookings")

        // Set up RecyclerView
        binding.bookingsRecyclerView.layoutManager = LinearLayoutManager(this)
        bookingList = arrayListOf()

        bookingAdapter = AdminBookingAdapter(
            bookings = bookingList,
            onAcceptClick = { booking -> updateBookingStatus(booking, "Accepted") },
            onPendingClick = { booking -> updateBookingStatus(booking, "Pending") },
            onEditClick = { booking -> editBookingDateAndTime(booking) },
            onDeleteClick = { booking -> deleteBooking(booking) }
        )
        binding.bookingsRecyclerView.adapter = bookingAdapter

        // Fetch bookings from database
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
                Toast.makeText(this@AdminManageAppointmentActivity, "Failed to load bookings", Toast.LENGTH_SHORT).show()
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
        val calendar = Calendar.getInstance()

        // Show DatePickerDialog
        DatePickerDialog(this, { _, year, month, dayOfMonth ->
            val selectedDate = "$year-${month + 1}-$dayOfMonth"

            // Show TimePickerDialog after date is selected
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
}