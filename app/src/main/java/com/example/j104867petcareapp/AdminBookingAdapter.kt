package com.example.j104867petcareapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.j104867petcareapp.databinding.ItemAdminBookingBinding
import com.example.j104867petcareapp.models.Booking

class AdminBookingAdapter(
    private val bookings: List<Booking>,
    private val onAcceptClick: (Booking) -> Unit,
    private val onPendingClick: (Booking) -> Unit,
    private val onEditClick: (Booking) -> Unit,
    private val onDeleteClick: (Booking) -> Unit
) : RecyclerView.Adapter<AdminBookingAdapter.BookingViewHolder>() {

    inner class BookingViewHolder(private val binding: ItemAdminBookingBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(booking: Booking) {
            binding.serviceNameTextView.text = booking.serviceName
            binding.bookingDateTextView.text = "Date: ${booking.bookingDate}"
            binding.bookingTimeTextView.text = "Time: ${booking.bookingTime}"
            binding.petNameTextView.text = "Pet Name: ${booking.petName}"
            binding.statusTextView.text = "Status: ${booking.status}"

            // Setting up click listeners for buttons
            binding.acceptButton.setOnClickListener { onAcceptClick(booking) }
            binding.pendingButton.setOnClickListener { onPendingClick(booking) }
            binding.editBookingButton.setOnClickListener { onEditClick(booking) }
            binding.deleteButton.setOnClickListener { onDeleteClick(booking) }

            // Manage button visibility and text based on the current booking status
            when (booking.status) {
                "Pending" -> {
                    binding.acceptButton.text = "Accept"
                    binding.acceptButton.visibility = View.VISIBLE
                    binding.pendingButton.visibility = View.GONE
                }
                "Accepted" -> {
                    binding.pendingButton.text = "Set to Pending"
                    binding.acceptButton.visibility = View.GONE
                    binding.pendingButton.visibility = View.VISIBLE
                }
                else -> {
                    binding.acceptButton.visibility = View.GONE
                    binding.pendingButton.visibility = View.GONE
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val binding = ItemAdminBookingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BookingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        holder.bind(bookings[position])
    }

    override fun getItemCount(): Int = bookings.size
}
