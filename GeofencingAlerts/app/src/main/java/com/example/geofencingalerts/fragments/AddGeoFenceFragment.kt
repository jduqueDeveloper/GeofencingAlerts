package com.example.geofencingalerts.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.room.Room
import com.example.geofencingalerts.Entities.AlertEntity
import com.example.geofencingalerts.Entities.GeoFenceEntity
import com.example.geofencingalerts.MapActivity
import com.example.geofencingalerts.database.GeoFenceDataBase
import com.example.geofencingalerts.databinding.FragmentAddGeoFenceBinding

class AddGeoFenceFragment : Fragment() {

    private lateinit var binding: FragmentAddGeoFenceBinding

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

            val newAlertEntity = AlertEntity(
                geoFenceName = binding.txtInputEditTextName.text.toString(),
                message = binding.txtInputEditTextMessage.text.toString(),
                CellPhone = binding.txtInputEditTextCellphone.text.toString()
            )

            db.alertEntityDao().insertAlertEntity(newAlertEntity)
            val intent = Intent(requireActivity(), MapActivity::class.java)
            startActivity(intent)
        }
    }
}