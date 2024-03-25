package ca.dal.cs.csci4176.group01undergraduate

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.content.Intent
import android.graphics.Color
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import ca.dal.cs.csci4176.group01undergraduate.addingbookbox.AddingBookBoxActivity
import ca.dal.cs.csci4176.group01undergraduate.displayingbookbox.BoxFragment
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import ca.dal.cs.csci4176.group01undergraduate.addingbookbox.models.BookBoxLocation
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
import com.squareup.picasso.Picasso
import java.io.IOException
import kotlin.properties.Delegates

class MapsFragment : Fragment(), OnMarkerClickListener{
    // variables to store current latitude and longitude
    var latitude by Delegates.notNull<Double>()
    var longitude by Delegates.notNull<Double>()

    // map
    private lateinit var map: GoogleMap

    // Halifax location
    private val northEast: LatLng = LatLng(44.684204, -63.543474)
    private val southWest: LatLng = LatLng(44.6209409, -63.629210)

    // maps
    private lateinit var bottomSheetBehavior : BottomSheetBehavior<LinearLayout>
    private lateinit var bottomSheetLayout: LinearLayout
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // polyline for directions
    private var currentPolyline: Polyline? = null

    // database
    private lateinit var database: FirebaseDatabase
    private lateinit var dbReference: DatabaseReference

    // bookbox
    private lateinit var betterBookBox: HashMap<String, BookBox>

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

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onMarkerClick(marker: Marker):Boolean {

        if(activity is MainActivity){
            val mainActivity = activity as MainActivity

            // find the bottomSheet
            val linearLayout = mainActivity
                .findViewById<LinearLayout>(R.id.bottomSheetLayout)


            betterBookBox.values.forEach {
                if (it.location != null) linearLayout.findViewById<TextView>(R.id.bookBoxLocation)
                    .text = getAddressFromLatLng(it.location.latitude, it.location.longitude)

                Picasso
                    .get()
                    .load(it.imageUrl)
                    .into(linearLayout.findViewById<ImageView>(R.id.bookBoxImage))

                // TODO(Add book for book box)
                linearLayout.findViewById<Button>(R.id.bookBoxAddBook)
                    .setOnClickListener{

                    }

                // find the button
                linearLayout.findViewById<Button>(R.id.getDirections)
                    .setOnClickListener{
                        getDirections(marker.position)
                    }
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
        betterBookBox.values.forEach { bookBox->
            if(bookBox.location != null){
                map.addMarker(
                    MarkerOptions()
                        .position(LatLng(bookBox.location.latitude, bookBox.location.longitude))
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_book_box))
                )
            }
        }
    }

    /**
     * gets address from lat lng
     */
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
                    return string
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

        // initialize hashmap
        betterBookBox = HashMap()

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
                        .getValue(Double::class.java)
                    val lng = bookBoxSnapshot.child("longitude")
                        .getValue(Double::class.java)

                    val description = bookBoxSnapshot.child("description")
                        .getValue(String::class.java)

                    val imageURL = bookBoxSnapshot.child("imageUrl")
                        .getValue(String::class.java)

                    if(lat !=null && lng !=null){
                        val bookBox = BookBox(name, BookBoxLocation(lat,lng),description,imageURL)

                        // add bookBox to hashmap
                        snapshot.key?.let { betterBookBox.put(it, bookBox) }

                        addMarkers()
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

        view.findViewById<Button>(R.id.findFav).setOnClickListener {
            // getting the firebase to find the users saved favourites
            var database: FirebaseDatabase = FirebaseDatabase.getInstance()
            var databaseReference: DatabaseReference = database.getReference("users")
            // array to hold the users favourite book boxes
            var favourites = emptyArray<String>()
            // gets the nearest favourite bookbox and displays its location
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
                map.isMyLocationEnabled = true
                getCurrentLocation()
            }
            // need to pass username through shared preferences to here
            databaseReference.child("username").get().addOnSuccessListener {
                // getting the favourites from the current user
                if (it.exists()) {
                    databaseReference = databaseReference.child("favourites")
                    databaseReference.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                for (contactSnap in snapshot.children) {
                                    val fav = contactSnap.value
                                    favourites += (fav!!).toString()
                                }
                            }
                        }
                        override fun onCancelled(error: DatabaseError) {
                            // error finding data
                        }
                    })
                }
            }
            var closest: String
            var minDistance: Double = 0.0
            // saves the coordinates of the closest bookbox
            var closeLong: Double = 0.0
            var closeLat: Double = 0.0
            // comparing the user favourites with the book boxes
            for (name in favourites) {
                databaseReference = FirebaseDatabase.getInstance().getReference("bookBoxes")
                // databaseReference.key
                databaseReference.child("name").child(name).get().addOnSuccessListener {
                    if (it.exists()) {
                        // get the latitude and longitude of the bookbox then calculate the distance, the one with the smallest distance gets displayed
                        var lat: Double = databaseReference.child("latitude").get().toString().toDouble()
                        var long: Double = databaseReference.child("latitude").get().toString().toDouble()
                        var distance: Double = getDistance(lat, long)
                        // if its the first or only fav then its set to be the closes box
                        if (minDistance == 0.0) {
                            closest = name
                            minDistance = distance
                            closeLong = long
                            closeLat = lat
                        }
                        // if the new book box had a closer distance then its now saved as such
                        if (distance < minDistance) {
                            closeLong = long
                            closeLat = lat
                            minDistance = distance
                            closest = name
                        }
                    }
                }
            }
            var outputTxt: String = "Nearby BookBox at: latitude: " + closeLat.toString() + " longitude: " + closeLong.toString()
            view.findViewById<TextView>(R.id.favLocation).setText(outputTxt)
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

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                // Got last known location. In some rare situations, this can be null.
                location?.let {
                    latitude = it.latitude
                    longitude = it.longitude
                }
            }
    }

    // has to be altered to account for negative long/lat values, calculates the distance between two points
    // add if statements to convert negatives to positives and get the difference in value
    private fun getDistance(lat: Double, long: Double): Double {
        var distance: Double
        if (lat < latitude) {
            distance = latitude - lat
        }
        else {
            distance = lat - latitude
        }
        if (long < longitude) {
            distance += longitude - long
        }
        else {
            distance += long - longitude
        }
        return distance
    }

}
