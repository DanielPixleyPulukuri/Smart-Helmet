package com.example.smarthelmet

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.smarthelmet.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkLocationPermission()

        binding.yes.setOnClickListener {
            intent = Intent(this, Driver::class.java)
            startActivity(intent)
        }

        binding.no.setOnClickListener {
            intent = Intent(this, NonDrivers::class.java)
            startActivity(intent)
        }

    }

    private fun checkLocationPermission() {

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            val intent = Intent(this, LocationPermission::class.java)
            startActivity(intent)
        } else {
            Toast.makeText(this, "Location Granted", Toast.LENGTH_SHORT).show()
        }

    }
}