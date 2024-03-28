package com.example.smarthelmet

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import android.graphics.Color
import android.location.Location
import android.os.Build
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import java.util.Arrays
import com.androidplot.xy.*
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.text.FieldPosition
import java.text.Format
import java.text.ParsePosition

class Driver : AppCompatActivity() {

    private lateinit var firebaseRef: DatabaseReference
    private lateinit var firebaseRefLocation: DatabaseReference
    private lateinit var plot: XYPlot
    private lateinit var normal:Button
    private lateinit var alert:Button
    private var latitude = 0.0
    private var longitude = 0.0
    private lateinit var deviceInfo: String

    private val domainLabels = arrayOf<Number>(93, 96, 97, 98, 99, 100, 101, 102, 103, 104)
    private var series1Number = arrayOf<Number>(95.0, 96.0, 101.0, 102.0, 97.0, 98.0, 99.0, 100.0, 101.0, 97.0)
    private lateinit var series1: XYSeries
    private lateinit var series1Format: LineAndPointFormatter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driver)

        firebaseRef = FirebaseDatabase.getInstance().getReference("sensor_data")
        val model = Build.MODEL
        deviceInfo = " $model"
        firebaseRefLocation = FirebaseDatabase.getInstance().getReference("Location")
        plot = findViewById(R.id.plot)
        alert = findViewById(R.id.alertButton)
        normal = findViewById(R.id.normalButton)
        alert.visibility = View.INVISIBLE
        normal.visibility = View.INVISIBLE

        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        }
        checkLocationPermission()
        updateLocation()
        setupPlot()
        getData()
        accidentAlert()
    }

    private fun updateLocation() {
        getLocation()
    }


    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            val intent = Intent(this, LocationPermission::class.java)
            startActivity(intent)
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLocation() {
        val fusedLocationClient: FusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                latitude = location.latitude
                longitude = location.longitude
//                Toast.makeText(this,"$latitude $longitude",Toast.LENGTH_SHORT).show()
                firebaseRefLocation.child(deviceInfo).setValue("$latitude $longitude")
            }
        }

    }

    private fun accidentAlert() {
        val blinkAnimation = AnimationUtils.loadAnimation(this, R.anim.blink)
        firebaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val accelerationX = snapshot.child("acceleration_x").value?.toString()?.toDoubleOrNull() ?: 0.0
                val accelerationY = snapshot.child("acceleration_y").value?.toString()?.toDoubleOrNull() ?: 0.0
//                Toast.makeText(this@Driver,"XY",Toast.LENGTH_SHORT).show()
                if (accelerationX<2.0 || accelerationY<2.0){
                    alert.clearAnimation()
                    normal.visibility = View.VISIBLE
                    alert.visibility = View.INVISIBLE
                }
                if (accelerationX>2.0 || accelerationY>2.0){
                    alert.visibility = View.VISIBLE
                    alert.startAnimation(blinkAnimation)
                    normal.visibility = View.INVISIBLE
                    getLocation()
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w(ContentValues.TAG, "Failed to read value.", error.toException())
            }
        })
    }
    private fun setupPlot() {
        series1 = SimpleXYSeries(Arrays.asList(*series1Number), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Pulse Value")
        series1Format = LineAndPointFormatter(Color.GREEN, Color.WHITE, null, null)

        plot.addSeries(series1, series1Format)
        plot.graph.getLineLabelStyle(XYGraphWidget.Edge.BOTTOM).format = object : Format() {
            override fun format(obj: Any?, toAppendTo: StringBuffer, pos: FieldPosition): StringBuffer {
                val i = Math.round((obj as Number).toFloat())
                return toAppendTo.append(domainLabels[i])
            }

            override fun parseObject(source: String?, pos: ParsePosition): Any? {
                return null
            }
        }
    }
    private fun getData() {
        firebaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val pulseSensorValue = snapshot.child("pulse_sensor_value").value
                pulseSensorValue?.let {
                    rearrange(series1Number)
                    series1Number[9] = it as Number
                    //Toast.makeText(this@Driver, "$it", Toast.LENGTH_SHORT).show()
                    plotGraph()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(ContentValues.TAG, "Failed to read value.", error.toException())
            }
        })
    }

    private fun rearrange(a: Array<Number>) {
        for (i in 0..a.size-2){
            a[i] = a[i+1]
        }
    }

    private fun plotGraph() {
        series1 = SimpleXYSeries(Arrays.asList(*series1Number), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Pulse Value")
        plot.clear()
        plot.addSeries(series1, series1Format)
        plot.graph.getLineLabelStyle(XYGraphWidget.Edge.BOTTOM).format = object : Format() {
            override fun format(obj: Any?, toAppendTo: StringBuffer, pos: FieldPosition): StringBuffer {
                val i = Math.round((obj as Number).toFloat())
                return toAppendTo.append(domainLabels[i])
            }

            override fun parseObject(source: String?, pos: ParsePosition): Any? {
                return null
            }
        }
        plot.redraw()
    }
}