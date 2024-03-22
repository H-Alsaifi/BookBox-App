package ca.dal.cs.csci4176.group01undergraduate

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.content.Intent
import android.graphics.Color
import android.location.Geocoder
import android.os.Build
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import ca.dal.cs.csci4176.group01undergraduate.addingbookbox.AddingBookBoxActivity
import ca.dal.cs.csci4176.group01undergraduate.displayingbookbox.BoxFragment
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import ca.dal.cs.csci4176.group01undergraduate.displayingbookbox.BookBox
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener

import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment

import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.maps.DirectionsApi
import com.google.maps.GeoApiContext
import com.google.maps.PendingResult
import com.google.maps.internal.PolylineEncoding
import com.google.maps.model.DirectionsResult
import com.google.maps.model.TravelMode
import java.io.IOException

class MapsFragment : Fragment(), OnMarkerClickListener{

    private lateinit var map: GoogleMap

    // Halifax location
    private val northEast: LatLng = LatLng(44.684204, -63.543474)
    private val southWest: LatLng = LatLng(44.6209409, -63.629210)
    private var places: ArrayList<LatLng> = ArrayList()

    // maps
    private lateinit var bottomSheetBehavior : BottomSheetBehavior<LinearLayout>
    private lateinit var bottomSheetLayout: LinearLayout
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // polyline for directions
    private var currentPolyline: Polyline? = null

    // database
    private lateinit var database: FirebaseDatabase
    private lateinit var dbReference: DatabaseReference
    private lateinit var bookBoxes: ArrayList<BookBox>

    /**
     * Acts as a callback
     */
    @SuppressLint("MissingPermission")
    private val callback = OnMapReadyCallback{

        map = it


        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            map.isMyLocationEnabled = true
            getLastKnownLocation()
        }
        map.setOnMarkerClickListener(this)

        addMarkers()



        map.uiSettings.isZoomControlsEnabled = true
    }

    @SuppressLint("MissingPermission")
    private fun getLastKnownLocation() {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                // Got last known location. In some rare situations, this can be null.
                location?.let {
                    val currentLatLng = LatLng(it.latitude, it.longitude)
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                }
            }
    }

    override fun onMarkerClick(marker: Marker):Boolean {

        if(activity is MainActivity){
            val mainActivity = activity as MainActivity

            // find the bottomSheet
            val linearLayout = mainActivity
                .findViewById<LinearLayout>(R.id.bottomSheetLayout)

            // find the text view
            linearLayout.findViewById<TextView>(R.id.coordinates)
                .text = marker.position.toString()

            // find the button
            linearLayout.findViewById<Button>(R.id.getDirections)
                .setOnClickListener{
                    getDirections(marker.position)
                }

            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }

        return true
    }


    /**
     * Gets the directions from api and shows on map
     */
    private fun requestDirection(destination: LatLng, source: LatLng){
        val geoApiContext = GeoApiContext.Builder()
            .apiKey("AIzaSyAza1wO0SS3GB207cYAZjkYWc-ugqQVCg4")
            .build()

        val request = DirectionsApi.newRequest(geoApiContext)
            .origin(source.latitude.toString() + "," + source.longitude.toString())
            .destination(destination.latitude.toString()+","+destination.longitude.toString())
            .mode(TravelMode.WALKING)

        request.setCallback(object : PendingResult.Callback<DirectionsResult>{
            override fun onResult(result: DirectionsResult?) {
                if (result?.routes?.isNotEmpty() == true){
                    val route = result.routes[0]
                    val decodedPath = PolylineEncoding.decode(route.overviewPolyline.encodedPath)
                    val points = ArrayList<LatLng>()
                    decodedPath.forEach { point ->
                        points.add(LatLng(point.lat, point.lng))
                    }

                    requireActivity().runOnUiThread {
                        currentPolyline?.remove()
                        val polyLineOptions = PolylineOptions()
                            .addAll(points).color(Color.RED)
                        currentPolyline = map.addPolyline(polyLineOptions)
                    }

                }
            }

            override fun onFailure(e: Throwable?) {
                requireActivity().runOnUiThread {
                    Toast
                        .makeText(requireContext(),
                            "Failure to fetch Directions!",
                            Toast.LENGTH_SHORT)
                        .show()
                }
            }
        })
    }

    /**
     * Gets current location and does a request via callback [requestDirection]
     */
    @SuppressLint("MissingPermission")
    private fun getDirections(destination: LatLng){
        fusedLocationClient.lastLocation.addOnSuccessListener{location ->
            location?.let {
                requestDirection(destination,
                    LatLng(it.latitude, it.longitude),)
            }
        }
    }

    /**
     * Add pins to map
     * icon taken from: https://www.figma.com/file/62O8YMjZOLkkTe9jqkmp61/coolicons-%7C-Free-Iconset-(Community)?type=design&t=Umarm5N5x9E9bXGJ-6
     */
    private fun addMarkers(){
        bookBoxes.forEach { bookBox->
            map.addMarker(
                MarkerOptions()
                    .position(LatLng(bookBox.location.latitude, bookBox.location.longitude))
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_book_box))
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun getAddressFromLatLng(lat: Double, lng: Double): String {
        val geocoder = Geocoder(requireContext())
        var string = ""
        try {
           val addresses = geocoder.getFromLocation(lat,lng,1)
            if (addresses != null) {
                if (addresses.isNotEmpty()) {
                    val address = addresses[0]
                    string = String.format("%s %s, %s, %s %s",
                        address.subThoroughfare,
                        address.thoroughfare,
                        address.subAdminArea,
                        address.adminArea,
                        address.postalCode)

                }
            }

        } catch (e: IOException){
            Log.d("Error", e.toString())
        }
        return string
    }

    /**
     * If [MapsFragment] successfully created assign some values
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bookBoxes = ArrayList()

        // database
        database = FirebaseDatabase.getInstance()
        dbReference = database.getReference("/bookBoxes")

        dbReference.addValueEventListener(object : ValueEventListener{
            @RequiresApi(Build.VERSION_CODES.TIRAMISU)
            override fun onDataChange(snapshot: DataSnapshot) {
                for (bookBoxSnapshot in snapshot.children){
                    val name = bookBoxSnapshot.child("name")
                        .getValue(String::class.java)

                    val lat = bookBoxSnapshot.child("latitude")
                        .getValue(String::class.java)?.toDouble()
                    val lng = bookBoxSnapshot.child("longitude")
                        .getValue(String::class.java)?.toDouble()

                    val description = bookBoxSnapshot.child("description")
                        .getValue(String::class.java)

                    val imageURL = bookBoxSnapshot.child("imageUrl")
                        .getValue(String::class.java)

//                    val latLng = location?.let { getLatLngFromAddress(it) }

                    if(lat !=null && lng !=null){
                       val address = getAddressFromLatLng(44.63847887747145, -63.58979199265351)
//                        Log.d("latlng from address", address)
                        val bookBox = BookBox(name,address,description,imageURL)
                        bookBoxes.add(bookBox)

                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to fetch data", Toast.LENGTH_SHORT).show()
            }

        })


    }

    /**
     * Inflates the map
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_interactive_map, container, false)





        return view
    }

    /**
     * Creates the maps itself
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // bottom sheet
        bottomSheetLayout = view.findViewById(R.id.bottomSheetLayout)
        bottomSheetBehavior = BottomSheetBehavior
            .from(bottomSheetLayout)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync{
            it.setOnMapLoadedCallback{
                val halifaxBounds = LatLngBounds
                    .builder()
                    .include(northEast)
                    .include(southWest)
                    .build()
                it.moveCamera(CameraUpdateFactory.newLatLngBounds(halifaxBounds, 10))
            }
        }
        mapFragment?.getMapAsync(callback)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        view.findViewById<Button>(R.id.view_list_button).setOnClickListener {
            // Navigate to BoxFragment to view the list
            navigateToBoxFragment()
        }

        // Set up button to add a new book box
        view.findViewById<Button>(R.id.add_bookbox_button).setOnClickListener {
            // Start AddingBookBoxActivity to add a new book box
            startActivity(Intent(context, AddingBookBoxActivity::class.java))
        }
    }
        private fun navigateToBoxFragment() {
            parentFragmentManager.beginTransaction().apply {
                replace(R.id.fragment_container, BoxFragment()) // Use the ID of your container where fragments are placed
                addToBackStack(null) // Add this transaction to the back stack
                commit() // Commit the transaction
            }
    }
}