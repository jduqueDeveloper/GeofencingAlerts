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
/*            geoFenceListDTO.geoFenceList?.add(
                GeoFence(
                    latLng = LatLng(
                        binding.textInputEditTextLatitud.text.toString().toDouble(),
                        binding.textInputEditTextLongitud.text.toString().toDouble()
                    ),
                    radius = binding.txtInputEditTextRadius.toString().toFloat(),
                    geoFenceName = binding.txtInputEditTextName.toString()
                )
            )
            geoFenceListDTO.alertList?.add(
                AlertList(
                    cellphone = binding.txtInputEditTextMessage.toString(),
                    message = binding.txtInputEditTextMessage.toString()
                )
            )*/

/*            val newGeoFence = GeoFenceEntity(
                uid =1,
                lat = binding.textInputEditTextLatitud.text.toString().toDouble(),
                lng = binding.textInputEditTextLongitud.text.toString().toDouble(),
                radius = binding.txtInputEditTextRadius.toString().toFloat(),
                geoFenceName = binding.txtInputEditTextName.toString()
            )*/

            val newGeoFence = GeoFenceEntity(
                lat = 6.143029,
                lng = -75.447239,
                radius = 20.0f,
                geoFenceName = "morroputo"
            )

            db.geoFenceEntityDao().insertGeoFenceEntity(newGeoFence)

        }
    }
}