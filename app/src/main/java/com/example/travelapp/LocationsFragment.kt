package com.example.travelapp

import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.codepath.asynchttpclient.AsyncHttpClient
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler
import com.example.travelapp.LocationAdapter
import com.example.travelapp.TravelLocation
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch
import okhttp3.Headers
import okhttp3.internal.http2.Header
import org.json.JSONObject
import android.Manifest

class LocationsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: LocationAdapter
    private val locationList = mutableListOf<TravelLocation>()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var database: AppDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        database = AppDatabase.getInstance(requireContext())
        val view = inflater.inflate(R.layout.fragment_locations, container, false)
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = LocationAdapter(
            locationList,
            { location -> saveFavorite(location) },
            {}
        )
        recyclerView.adapter = adapter
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        requestLocationPermissionAndFetch()
        return view
    }
    private fun requestLocationPermissionAndFetch() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            fetchCurrentLocation()
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            fetchCurrentLocation()
        } else {
            Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show()
        }
    }
    private fun fetchCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    fetchNearbyLocations(location.latitude, location.longitude)
                } else {
                    Toast.makeText(requireContext(), "Failed to get location", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    private fun fetchNearbyLocations(latitude: Double, longitude: Double) {
        val url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json" +
                "?location=$latitude,$longitude" +
                "&radius=1000" +
                "&type=tourist_attraction" +
                "&key=AIzaSyCpCxxcok5crmDllmFvzNLb2XorHb1pclM"

        val client = AsyncHttpClient()
        client.get(url, object : JsonHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Headers, json: JSON) {
                val response = json.jsonObject
                response?.getJSONArray("results")?.let { results ->
                    locationList.clear()
                    for (i in 0 until results.length()) {
                        val item = results.getJSONObject(i)
                        val location = TravelLocation(
                            name = item.getString("name"),
                            address = item.getString("vicinity"),
                            rating = item.optDouble("rating", 0.0)
                        )
                        locationList.add(location)
                    }
                    adapter.notifyDataSetChanged()
                }
            }

            override fun onFailure(
                statusCode: Int,
                headers: Headers?,
                response: String?,
                throwable: Throwable?
            ) {
                Toast.makeText(requireContext(), "Failed to fetch nearby locations", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun saveFavorite(location: TravelLocation) {
        lifecycleScope.launch {
            val favoriteEntity = FavoriteEntity(location.name, location.address, location.rating)
            database.favoriteDao().insertFavorite(favoriteEntity)
            Toast.makeText(requireContext(), "${location.name} added to favorites", Toast.LENGTH_SHORT).show()
        }
    }
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }
}