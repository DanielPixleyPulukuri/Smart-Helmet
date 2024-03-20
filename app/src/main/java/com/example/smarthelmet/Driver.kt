package com.example.smarthelmet

import android.content.ContentValues
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import android.graphics.Color
import android.widget.Toast
import java.util.Arrays
import com.androidplot.xy.*
import java.text.FieldPosition
import java.text.Format
import java.text.ParsePosition

class Driver : AppCompatActivity() {

    private lateinit var firebaseRef: DatabaseReference
    private lateinit var plot: XYPlot

    private val domainLabels = arrayOf<Number>(93, 96, 97, 98, 99, 100, 101, 102, 103, 104)
    private var series1Number = arrayOf<Number>(95.0, 96.0, 101.0, 102.0, 97.0, 98.0, 99.0, 100.0, 101.0, 97.0)
    private lateinit var series1: XYSeries
    private lateinit var series1Format: LineAndPointFormatter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driver)

        firebaseRef = FirebaseDatabase.getInstance().getReference("sensor_data")
        plot = findViewById(R.id.plot)

        setupPlot()
        getData()
    }
    private fun setupPlot() {
        series1 = SimpleXYSeries(Arrays.asList(*series1Number), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Series 1")
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
                    series1Number[9] = it as Number
                    Toast.makeText(this@Driver, "$it", Toast.LENGTH_SHORT).show()
                    plotGraph()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(ContentValues.TAG, "Failed to read value.", error.toException())
            }
        })
    }
    private fun plotGraph() {
        series1 = SimpleXYSeries(Arrays.asList(*series1Number), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Series 1")
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