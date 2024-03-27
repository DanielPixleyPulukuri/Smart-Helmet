package com.example.smarthelmet

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.androidplot.xy.LineAndPointFormatter
import com.androidplot.xy.SimpleXYSeries
import com.androidplot.xy.XYGraphWidget
import com.androidplot.xy.XYPlot
import com.androidplot.xy.XYSeries
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.FieldPosition
import java.text.Format
import java.text.ParsePosition
import java.util.Arrays

class NonDrivers : AppCompatActivity() {

    private lateinit var firebaseRef: DatabaseReference
    private lateinit var firebaseRefLocation: DatabaseReference
    private lateinit var plot: XYPlot
    private lateinit var normal: Button
    private lateinit var alert: Button
    private lateinit var location: Button
    private lateinit var locationAxix: String
    private var latitude = 0.0
    private var longitude = 0.0
    private val domainLabels = arrayOf<Number>(93, 96, 97, 98, 99, 100, 101, 102, 103, 104)
    private var series1Number =
        arrayOf<Number>(95.0, 96.0, 101.0, 102.0, 97.0, 98.0, 99.0, 100.0, 101.0, 97.0)
    private lateinit var series1: XYSeries
    private lateinit var series1Format: LineAndPointFormatter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_non_drivers)

        firebaseRef = FirebaseDatabase.getInstance().getReference("sensor_data")
        firebaseRefLocation = FirebaseDatabase.getInstance().getReference("location")
        plot = findViewById(R.id.plot)
        alert = findViewById(R.id.alertButton)
        normal = findViewById(R.id.normalButton)
        location = findViewById(R.id.getLocation)
        alert.visibility = View.INVISIBLE
        location.visibility = View.INVISIBLE

        setupPlot()
        getData()
        accidentAlert()
    }

    private fun accidentAlert() {
        val blinkAnimation = AnimationUtils.loadAnimation(this, R.anim.blink)
        firebaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val accelerationX =
                    snapshot.child("acceleration_x").value?.toString()?.toDoubleOrNull() ?: 0.0
                val accelerationY =
                    snapshot.child("acceleration_y").value?.toString()?.toDoubleOrNull() ?: 0.0
//                Toast.makeText(this@NonDrivers, "XY", Toast.LENGTH_SHORT).show()
                if (accelerationX <= 2.0 || accelerationY < 2.0) {
                    normal.visibility = View.VISIBLE
                    alert.visibility = View.INVISIBLE
                    location.visibility = View.INVISIBLE
                } else {
                    alert.visibility = View.VISIBLE
                    alert.startAnimation(blinkAnimation)
                    normal.visibility = View.INVISIBLE
                    location.visibility = View.VISIBLE

                    location.setOnClickListener {
                        getLocation()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(ContentValues.TAG, "Failed to read value.", error.toException())
            }
        })
    }

    private fun getLocation() {
        firebaseRefLocation.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                locationAxix = snapshot.value.toString()
                val numberStrings = locationAxix.split(" ") // Split by space
                val numbers = numberStrings.map { it.toDouble() } // Convert to Int
//                Toast.makeText(this@NonDrivers,"${numbers[0]}",Toast.LENGTH_SHORT).show()
                latitude = numbers[0]
                longitude = numbers[1]
                maps(latitude,longitude)
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun maps(latitude: Double, longitude: Double) {
        val uri = Uri.parse("geo:0,0?q=$latitude+$longitude")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.setPackage("com.google.android.apps.maps")
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    private fun setupPlot() {
        series1 = SimpleXYSeries(
            Arrays.asList(*series1Number), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Pulse Value"
        )
        series1Format = LineAndPointFormatter(Color.GREEN, Color.WHITE, null, null)

        plot.addSeries(series1, series1Format)
        plot.graph.getLineLabelStyle(XYGraphWidget.Edge.BOTTOM).format = object : Format() {
            override fun format(
                obj: Any?, toAppendTo: StringBuffer, pos: FieldPosition
            ): StringBuffer {
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

    private fun plotGraph() {
        series1 = SimpleXYSeries(
            Arrays.asList(*series1Number), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Pulse Value"
        )
        plot.clear()
        plot.addSeries(series1, series1Format)
        plot.graph.getLineLabelStyle(XYGraphWidget.Edge.BOTTOM).format = object : Format() {
            override fun format(
                obj: Any?, toAppendTo: StringBuffer, pos: FieldPosition
            ): StringBuffer {
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