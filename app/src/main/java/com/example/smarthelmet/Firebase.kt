package com.example.smarthelmet

import android.content.ContentValues
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Firebase {
    private lateinit var firebaseRef: DatabaseReference

    fun connect(){
        firebaseRef = FirebaseDatabase.getInstance().getReference("sensor_data")
    }
    fun getData(){
        firebaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
//                val vibrationState = snapshot.child("vibration_state").value
//                val alcoholValue = snapshot.child("alcohol_value").value
//                val pulseSensorValue = snapshot.child("pulse_sensor_value").value
//                val accelerationX = snapshot.child("acceleration_x").value
//                val accelerationY = snapshot.child("acceleration_y").value
//                val accelerationZ = snapshot.child("acceleration_z").value

            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(ContentValues.TAG, "Failed to read value.", error.toException())
            }

        })
    }
}