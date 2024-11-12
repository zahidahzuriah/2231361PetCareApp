package com.example.j104867petcareapp

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.j104867petcareapp.databinding.ActivityUserDashboardBinding
import com.example.j104867petcareapp.models.Service
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class UserDashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUserDashboardBinding
    private lateinit var database: DatabaseReference
    private lateinit var serviceList: ArrayList<Service>
    private lateinit var filteredServiceList: ArrayList<Service>
    private lateinit var serviceAdapter: ServiceAdapter
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize View Binding
        binding = ActivityUserDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Initialize Firebase Database reference for services
        database = FirebaseDatabase.getInstance().reference.child("Services")

        // Initialize RecyclerView
        binding.servicesRecyclerView.layoutManager = LinearLayoutManager(this)
        serviceList = arrayListOf()
        filteredServiceList = arrayListOf()

        // Set up adapter with click listener for each service
        serviceAdapter = ServiceAdapter(
            services = filteredServiceList,
            isAdmin = false, // Set to false as this is for users
            onEditClick = { service ->
                // Users do not have permission to edit services
                Toast.makeText(this, "You do not have permission to edit services.", Toast.LENGTH_SHORT).show()
            },
            onDeleteClick = { service ->
                // Users do not have permission to delete services
                Toast.makeText(this, "You do not have permission to delete services.", Toast.LENGTH_SHORT).show()
            }
        )

        // Set the click listener to navigate to the booking page
        serviceAdapter.setOnServiceClickListener { service ->
            val intent = Intent(this, BookServiceActivity::class.java)
            intent.putExtra("serviceId", service.serviceId)
            intent.putExtra("serviceName", service.name)
            intent.putExtra("servicePrice", service.price) // Pass service price
            intent.putExtra("serviceDescription", service.description) // Pass service description
            startActivity(intent)
        }

        binding.servicesRecyclerView.adapter = serviceAdapter

        // Fetch services from database
        fetchServices()

        // Set up search functionality
        binding.serviceSearchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterServices(newText ?: "")
                return true
            }
        })
    }

    private fun fetchServices() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                serviceList.clear()
                for (serviceSnapshot in snapshot.children) {
                    val service = serviceSnapshot.getValue(Service::class.java)
                    if (service != null) {
                        serviceList.add(service)
                    }
                }
                // Initially show all services in the filtered list
                filteredServiceList.clear()
                filteredServiceList.addAll(serviceList)
                serviceAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@UserDashboardActivity, "Failed to load services", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun filterServices(query: String) {
        val searchText = query.lowercase()
        filteredServiceList.clear()

        if (searchText.isEmpty()) {
            filteredServiceList.addAll(serviceList)
        } else {
            for (service in serviceList) {
                if (service.name.lowercase().contains(searchText) ||
                    service.description.lowercase().contains(searchText)) {
                    filteredServiceList.add(service)
                }
            }
        }
        serviceAdapter.notifyDataSetChanged()
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
