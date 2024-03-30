package com.example.smarthelmet

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue

class MapsFragment : Fragment() {

    private lateinit var firebaseRefLocation: DatabaseReference
    private lateinit var locationAxix: String
    private var latitude = 0.0
    private var longitude = 0.0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_maps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseRefLocation = FirebaseDatabase.getInstance().getReference("Location")
        getLocation(view)
    }

    private fun getLocation(view: View) {
        firebaseRefLocation.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                locationAxix = snapshot.getValue<String>().toString()

                Toast.makeText(requireContext(), locationAxix, Toast.LENGTH_SHORT).show()
                val numberStrings = locationAxix.split(" ") // Split by space
                val numbers = numberStrings.map { it.toDouble() } // Convert to Int
                latitude = numbers[0]
                longitude = numbers[1]
                Toast.makeText(requireContext(), "$longitude", Toast.LENGTH_SHORT).show()

                maps(latitude,longitude,view)
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun maps(latitude: Double, longitude: Double, view: View) {
        val webView: WebView = view.findViewById(R.id.webView)
        webView.webViewClient = WebViewClient()
        val webSettings: WebSettings = webView.settings
        webSettings.javaScriptEnabled = true
        val mapUrl = "https://www.google.com/maps?q=$latitude,$longitude"
        webView.loadUrl(mapUrl)

    }

}