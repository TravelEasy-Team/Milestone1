package com.example.travelapp

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.travelapp.LocationAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.launch
import androidx.lifecycle.Observer

class FavoritesFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: LocationAdapter
    private lateinit var database: AppDatabase
    private val favoriteList = mutableListOf<TravelLocation>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_favorites, container, false)
        recyclerView = view.findViewById(R.id.recyclerViewFavorites)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = LocationAdapter(favoriteList, { location -> removeFavorite(location) }, { location -> confirmDelete(location) })
        recyclerView.adapter = adapter
        database = AppDatabase.getInstance(requireContext())
        fetchFavorites()
        return view
    }

    private fun fetchFavorites() {
        database.favoriteDao().getAllFavorites().observe(viewLifecycleOwner, Observer { favorites ->
            favoriteList.clear()
            favoriteList.addAll(favorites.map {
                TravelLocation(it.name, it.address, it.rating)
            })
            adapter.notifyDataSetChanged()
        })
    }

    private fun removeFavorite(location: TravelLocation) {
        lifecycleScope.launch {
            val favoriteEntity = FavoriteEntity(location.name, location.address, location.rating)
            database.favoriteDao().deleteFavorite(favoriteEntity)
            Toast.makeText(requireContext(), "${location.name} removed from favorites", Toast.LENGTH_SHORT).show()
        }
    }
    private fun confirmDelete(location: TravelLocation) {
        val alertDialog = android.app.AlertDialog.Builder(requireContext())
            .setTitle("Delete Favorite")
            .setMessage("Are you sure you want to delete ${location.name} from favorites?")
            .setPositiveButton("Yes") { _, _ -> removeFavorite(location) }
            .setNegativeButton("No", null)
            .create()
        alertDialog.show()
    }
}
