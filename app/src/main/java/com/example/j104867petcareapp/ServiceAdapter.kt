package com.example.j104867petcareapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.j104867petcareapp.models.Service

class ServiceAdapter(
    private val services: List<Service>,
    private val isAdmin: Boolean,
    private val onEditClick: (Service) -> Unit,
    private val onDeleteClick: (Service) -> Unit
) : RecyclerView.Adapter<ServiceAdapter.ServiceViewHolder>() {

    private var onServiceClickListener: ((Service) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_service, parent, false)
        return ServiceViewHolder(view)
    }

    override fun onBindViewHolder(holder: ServiceViewHolder, position: Int) {
        val service = services[position]
        holder.bind(service)
    }

    override fun getItemCount(): Int = services.size

    fun setOnServiceClickListener(listener: (Service) -> Unit) {
        onServiceClickListener = listener
    }

    inner class ServiceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val serviceNameTextView: TextView = itemView.findViewById(R.id.serviceNameTextView)
        private val serviceDescriptionTextView: TextView = itemView.findViewById(R.id.serviceDescriptionTextView)
        private val servicePriceTextView: TextView = itemView.findViewById(R.id.servicePriceTextView)
        private val editButton: Button? = itemView.findViewById(R.id.editServiceButton)  // Corrected ID reference
        private val deleteButton: Button? = itemView.findViewById(R.id.deleteServiceButton) // Corrected ID reference
        private val actionButtonsLayout: View? = itemView.findViewById(R.id.actionButtonsLayout)

        fun bind(service: Service) {
            serviceNameTextView.text = service.name
            serviceDescriptionTextView.text = service.description
            servicePriceTextView.text = "Price: $${service.price}"

            // Show edit and delete buttons only for admins
            if (isAdmin) {
                actionButtonsLayout?.visibility = View.VISIBLE
                editButton?.setOnClickListener {
                    onEditClick(service)
                }
                deleteButton?.setOnClickListener {
                    onDeleteClick(service)
                }
            } else {
                actionButtonsLayout?.visibility = View.GONE
            }

            // Set item click listener
            itemView.setOnClickListener {
                onServiceClickListener?.invoke(service)
            }
        }
    }
}
