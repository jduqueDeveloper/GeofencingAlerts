package com.example.geofencingalerts

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.DataBindingUtil.inflate
import androidx.navigation.fragment.findNavController
import com.example.geofencingalerts.databinding.FragmentStartBinding

class StartFragment : Fragment() {
    private lateinit var binding: FragmentStartBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStartBinding.inflate(inflater)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        binding.buttonAddGeoAlert.setOnClickListener {
            findNavController().navigate(R.id.addGeoFenceFragment)
        }

        binding.buttonViewMap.setOnClickListener {
           // findNavController().navigate(R.id.mapFragment)
            val intent = Intent(requireActivity(),MapActivity::class.java)
            startActivity(intent)
        }
    }

}