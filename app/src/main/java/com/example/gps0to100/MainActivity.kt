package com.example.gps0to100

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.widget.Button
import android.widget.Chronometer
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

    private val REQUEST_LOCATION_PERMISSION = 1

    private lateinit var speedTextView: TextView
    private lateinit var chronometerView: Chronometer
    private lateinit var startButton: Button
    private var isPruebaStarted = false

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        speedTextView = findViewById(R.id.speedTextView)
        chronometerView = findViewById(R.id.chronometerView)
        startButton = findViewById(R.id.startButton)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location = locationResult.lastLocation
                updateSpeedUI(location)
            }
        }

        startButton.setOnClickListener {
            if (!isPruebaStarted) {
                startPrueba()
            } else {
                stopPrueba()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        startLocationUpdates()
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    private fun startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val locationRequest = LocationRequest.create()
            locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            locationRequest.interval = 1000 // Actualización de ubicación cada 1 segundo
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null)
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_PERMISSION)
        }
    }

    private fun stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    private fun startPrueba() {
        isPruebaStarted = true
        startButton.text = "Detener Prueba"
        chronometerView.visibility = View.VISIBLE
        chronometerView.base = SystemClock.elapsedRealtime()
        chronometerView.start()
    }

    private fun stopPrueba() {
        isPruebaStarted = false
        startButton.text = "Empezar Prueba"
        chronometerView.stop()
        chronometerView.visibility = View.GONE
    }

    private fun updateSpeedUI(location: Location?) {
        if (location != null) {
            val speed = (location.speed * 3.6).roundToInt() // m/s a km/h y redondear
            speedTextView.text = "Velocidad: $speed km/h"

            if (isPruebaStarted && speed >= 2) {
                stopPrueba()
                Toast.makeText(this, "Prueba detenida: velocidad igual o mayor a 2 km/h", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates()
            } else {
                speedTextView.text = "Permiso de ubicación denegado"
            }
        }
    }
}
