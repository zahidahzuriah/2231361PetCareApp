package com.example.j104867petcareapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.j104867petcareapp.databinding.ActivityViewServicesBinding
import com.example.j104867petcareapp.models.Service
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ViewServicesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityViewServicesBinding
    private lateinit var databaseServices: DatabaseReference
    private lateinit var servicesList: ArrayList<Service>
    private lateinit var serviceAdapter: ServiceAdapter
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize View Binding
        binding = ActivityViewServicesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase references
        databaseServices = FirebaseDatabase.getInstance().reference.child("Services")
        servicesList = arrayListOf()
        auth = FirebaseAuth.getInstance()

        // Setup RecyclerView
        binding.servicesRecyclerView.layoutManager = LinearLayoutManager(this)

        // Determine if the user is an admin
        val isAdmin = checkIfAdmin()

        // Initialize the service adapter
        serviceAdapter = ServiceAdapter(
            services = servicesList,
            isAdmin = isAdmin,
            onEditClick = { service -> showEditServiceDialog(service) },
            onDeleteClick = { service -> showDeleteConfirmationDialog(service) }
        )
        binding.servicesRecyclerView.adapter = serviceAdapter

        // Fetch the list of services
        fetchServices()

        // Set up Add Service button for Admins
        if (isAdmin) {
            binding.addServiceButton.visibility = View.VISIBLE
            binding.addServiceButton.setOnClickListener {
                showAddServiceDialog()
            }

            // Set up the Back Button for Admins
            binding.adminBackButton.visibility = View.VISIBLE
            binding.adminBackButton.setOnClickListener {
                // Go back to Admin Dashboard
                val intent = Intent(this, AdminDashboardActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    private fun checkIfAdmin(): Boolean {
        // This function should return true if the current user is an admin
        // Replace this with your actual logic to determine if the user is an admin
        return true // Placeholder value for demonstration purposes
    }

    private fun fetchServices() {
        databaseServices.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                servicesList.clear()
                for (serviceSnapshot in snapshot.children) {
                    val service = serviceSnapshot.getValue(Service::class.java)
                    if (service != null) {
                        servicesList.add(service)
                    }
                }
                serviceAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ViewServicesActivity, "Failed to load services", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showAddServiceDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_service, null)
        val editName = dialogView.findViewById<EditText>(R.id.editServiceNameEditText)
        val editDescription = dialogView.findViewById<EditText>(R.id.editServiceDescriptionEditText)
        val editPrice = dialogView.findViewById<EditText>(R.id.editServicePriceEditText)

        // Show dialog to add a new service
        AlertDialog.Builder(this)
            .setTitle("Add Service")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                // Get values from EditText fields
                val serviceName = editName.text.toString().trim()
                val serviceDescription = editDescription.text.toString().trim()
                val servicePriceText = editPrice.text.toString().trim()

                if (serviceName.isEmpty() || serviceDescription.isEmpty() || servicePriceText.isEmpty()) {
                    Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                // Convert price to Double
                val servicePrice = servicePriceText.toDoubleOrNull()
                if (servicePrice == null) {
                    Toast.makeText(this, "Please enter a valid price", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                // Add service to the database
                addService(serviceName, serviceDescription, servicePrice)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun addService(name: String, description: String, price: Double) {
        val serviceId = databaseServices.push().key ?: return
        val service = Service(serviceId, name, description, price)

        databaseServices.child(serviceId).setValue(service).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Service added successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Failed to add service", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showEditServiceDialog(service: Service) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_service, null)
        val editName = dialogView.findViewById<EditText>(R.id.editServiceNameEditText)
        val editDescription = dialogView.findViewById<EditText>(R.id.editServiceDescriptionEditText)
        val editPrice = dialogView.findViewById<EditText>(R.id.editServicePriceEditText)

        // Pre-fill with current values
        editName.setText(service.name)
        editDescription.setText(service.description)
        editPrice.setText(service.price.toString())

        // Show dialog to edit a service
        AlertDialog.Builder(this)
            .setTitle("Edit Service")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                // Get updated values
                val updatedName = editName.text.toString().trim()
                val updatedDescription = editDescription.text.toString().trim()
                val updatedPrice = editPrice.text.toString().toDoubleOrNull() ?: service.price

                // Update service in the database
                updateServiceInDatabase(service, updatedName, updatedDescription, updatedPrice)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateServiceInDatabase(service: Service, name: String, description: String, price: Double) {
        val updatedService = service.copy(name = name, description = description, price = price)
        databaseServices.child(service.serviceId).setValue(updatedService)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Toast.makeText(this, "Service updated successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to update service", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun showDeleteConfirmationDialog(service: Service) {
        AlertDialog.Builder(this)
            .setTitle("Delete Service")
            .setMessage("Are you sure you want to delete ${service.name}?")
            .setPositiveButton("Yes") { _, _ ->
                deleteService(service)
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun deleteService(service: Service) {
        databaseServices.child(service.serviceId).removeValue().addOnCompleteListener {
            if (it.isSuccessful) {
                Toast.makeText(this, "Service deleted successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Failed to delete service", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
