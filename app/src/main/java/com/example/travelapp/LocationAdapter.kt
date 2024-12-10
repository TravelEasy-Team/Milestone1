package com.example.travelapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.travelapp.R

class LocationAdapter(
    private val locations: List<TravelLocation>,
    private val onFavoriteClick: (TravelLocation) -> Unit,
    private val onLongClick: (TravelLocation) -> Unit
) : RecyclerView.Adapter<LocationAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.tvName)
        val address: TextView = view.findViewById(R.id.tvAddress)
        val rating: TextView = view.findViewById(R.id.tvRating)
        val favoriteButton: Button = view.findViewById(R.id.btnFavorite)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_location, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val location = locations[position]
        holder.name.text = location.name
        holder.address.text = location.address
        holder.rating.text = "Rating: ${location.rating}"
        holder.favoriteButton.setOnClickListener {
            onFavoriteClick(location)
        }
        holder.itemView.setOnLongClickListener {
            onLongClick(location)
            true
        }
    }

    override fun getItemCount() = locations.size
}