package com.example.j104867petcareapp.models

data class Booking(
  val bookingId: String = "",
  val serviceId: String = "",
  val serviceName: String = "",
  val bookingDate: String = "",
  val bookingTime: String = "",
  val petName: String = "",
  val petOwnerName: String = "",
  val userId: String = "",
  val status: String = "Pending",
  val bookingDateTime: String = "" // New field to store combined date and time
)
