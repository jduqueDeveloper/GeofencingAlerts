package com.example.geofencingalerts

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.room.Room
import com.example.geofencingalerts.databinding.FragmentAddGeoFenceBinding
import com.example.geofencingalerts.databinding.FragmentStartBinding
import com.google.android.libraries.maps.model.LatLng

class AddGeoFenceFragment : Fragment() {

    private lateinit var binding: FragmentAddGeoFenceBinding
    private lateinit var geoFenceListDTO: GeoFenceListDTO
    private lateinit var geoFence: GeoFence

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddGeoFenceBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val db = Room.databaseBuilder(
            requireContext(),
            GeoFenceDataBase::class.java, "database-geoFence"
        ).allowMainThreadQueries().build()

        binding.buttonAddGeofenceAlert.setOnClickListener {

            val newGeoFence = GeoFenceEntity(
                lat = binding.textInputEditTextLatitud.text.toString().toDouble(),
                lng = binding.textInputEditTextLongitud.text.toString().toDouble(),
                radius = binding.txtInputEditTextRadius.text.toString().toFloat(),
                geoFenceName = binding.txtInputEditTextName.text.toString()
            )

            db.geoFenceEntityDao().insertGeoFenceEntity(newGeoFence)

        }
    }
}