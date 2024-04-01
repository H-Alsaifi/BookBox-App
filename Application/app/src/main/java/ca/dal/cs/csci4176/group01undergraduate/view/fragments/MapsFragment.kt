package ca.dal.cs.csci4176.group01undergraduate.view.fragments

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
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import ca.dal.cs.csci4176.group01undergraduate.view.activities.AddingBookBoxActivity
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import ca.dal.cs.csci4176.group01undergraduate.R
import ca.dal.cs.csci4176.group01undergraduate.coords
import ca.dal.cs.csci4176.group01undergraduate.model.BookBoxLocation
import ca.dal.cs.csci4176.group01undergraduate.model.BookBox
import ca.dal.cs.csci4176.group01undergraduate.view.activities.AddBookActivity
import ca.dal.cs.csci4176.group01undergraduate.view.activities.MainActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener

import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment

import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.auth.FirebaseAuth
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
import java.util.Locale
import kotlin.properties.Delegates

class MapsFragment : Fragment(), OnMarkerClickListener{
    // variables to store current latitude and longitude
    var latitude by Delegates.notNull<Double>()
    var longitude by Delegates.notNull<Double>()

    // map
    private lateinit var map: GoogleMap
    private var isMapInitialized = false


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

    private var markerBookBoxIdMap = HashMap<Marker, String>()

    // location permission
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                // Permission is granted, proceed with location-related tasks
                getCurrentLocation()
            } else {
                parentFragmentManager
                    .beginTransaction()
                    .replace(R.id.fragment_container, MapErrorFragment())
                    .commit()
            }
        }

    /**
     * Acts as a callback
     */
    private val callback = OnMapReadyCallback{

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestLocationPermission()
        } else {
            getCurrentLocation()
        }

        map = it
        isMapInitialized = true
        tryAddingMarkers()

        map.setOnMarkerClickListener(this)

        map.uiSettings.isZoomControlsEnabled = true
    }

    private fun requestLocationPermission() {
        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private fun tryAddingMarkers() {
        if (isMapInitialized && betterBookBox.isNotEmpty()) {
            addMarkers()
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
                    val currentLatLng = LatLng(it.latitude, it.longitude)
                    map.isMyLocationEnabled = true
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))

                }
            }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onMarkerClick(marker: Marker): Boolean {
        val mainActivity = activity as? MainActivity ?: return true

        // Retrieve the book box ID associated with the clicked marker
        val bookBoxId = markerBookBoxIdMap[marker]
        val bookBox = bookBoxId?.let { betterBookBox[it] }

        bookBox?.let { box ->
            // Find the bottomSheet
            val linearLayout = mainActivity.findViewById<LinearLayout>(R.id.bottomSheetLayout)
            linearLayout.findViewById<TextView>(R.id.bookBoxLocation).text =
                box.location?.let { box.address }

            // Load the image with Picasso and use a callback to handle success and error
            Picasso.get().load(box.imageUrl).into(linearLayout.findViewById<ImageView>(R.id.bookBoxImage), object : com.squareup.picasso.Callback {
                override fun onSuccess() {
                    // Image successfully loaded, now you can proceed to show the bottom sheet
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

                    // Set up the "Add book for book box" button click listener
                    linearLayout.findViewById<Button>(R.id.bookBoxAddBook).setOnClickListener {
                        val intent = Intent(context, AddBookActivity::class.java).apply {
                            putExtra("BOOK_BOX_KEY", bookBoxId)
                        }
                        startActivity(intent)
                    }

                    // Set up the "View Books" button click listener
                    val viewBooksButton: Button = bottomSheetLayout.findViewById(R.id.viewBooksButton)
                    viewBooksButton.setOnClickListener {
                        navigateToBookListFragment(bookBoxId)
                    }

                    // Set up the "Get directions" button click listener
                    linearLayout.findViewById<Button>(R.id.getDirections).setOnClickListener {
                        box.location?.let { it1 -> LatLng(it1.latitude, box.location.longitude) }
                            ?.let { it2 -> getDirections(it2) }
                    }
                }

                override fun onError(e: Exception?) {
                    // Handle the error case, e.g., show a placeholder image or an error message
                    Toast.makeText(context, "Error loading image", Toast.LENGTH_SHORT).show()
                }
            })

            // Return true to indicate we have handled the marker click event
            return true
        }
        return false
    }


    private fun navigateToBookListFragment(bookBoxId: String) {
        val fragment = BookListFragment.newInstance(bookBoxId)
        parentFragmentManager.beginTransaction().apply {
            replace(R.id.fragment_container, fragment) // Use the ID of your container where fragments are placed
            addToBackStack(null) // Add this transaction to the back stack
            commit() // Commit the transaction
        }
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
    private fun addMarkers() {

        // Optional: Clear existing markers if needed
        map.clear()
        markerBookBoxIdMap.clear()

        // Iterate over each book box and add a marker for it
        betterBookBox.forEach { (bookBoxId, bookBox) ->
            if (bookBox.location != null) {
                val marker = map.addMarker(
                    MarkerOptions()
                        .position(LatLng(bookBox.location.latitude, bookBox.location.longitude))
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_book_box)) // Ensure you have this drawable resource
                )
                // Associate this marker with the book box's ID
                marker?.let {
                    markerBookBoxIdMap[it] = bookBoxId
                }
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
                betterBookBox.clear() // Clear the existing entries to avoid duplicates
                for (bookBoxSnapshot in snapshot.children) {
                    val bookBoxId = bookBoxSnapshot.key // Unique key for each book box
                    val lat = bookBoxSnapshot.child("latitude").getValue(Double::class.java)
                    val lng = bookBoxSnapshot.child("longitude").getValue(Double::class.java)
                    val address = bookBoxSnapshot.child("address").getValue((String::class.java))
                    val description = bookBoxSnapshot.child("description").getValue(String::class.java)
                    val imageURL = bookBoxSnapshot.child("imageUrl").getValue(String::class.java)
                    val bookIDs = bookBoxSnapshot.child("bookIDs").children.mapNotNull { it.key }.toMutableList()

                    if (lat != null && lng != null && bookBoxId != null) {
                        val location = BookBoxLocation(lat, lng)
                        val bookBox = BookBox(location, address, description, imageURL, bookIDs)
                        betterBookBox[bookBoxId] = bookBox // Use the unique key for each book box
                    }
                }
                tryAddingMarkers() // Attempt to add markers if map is ready
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

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        // Location permission
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 99)
        }

        // bottom sheet
        bottomSheetLayout = view.findViewById(R.id.bottomSheetLayout)
        bottomSheetBehavior = BottomSheetBehavior
            .from(bottomSheetLayout)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)

        view.findViewById<Button>(R.id.view_list_button).setOnClickListener {
            // Navigate to BoxFragment to view the list
            navigateToBoxFragment()
        }


        // getting the firebase to find the users saved favourites
        view.findViewById<Button>(R.id.findFav).setOnClickListener {
            var database: FirebaseDatabase = FirebaseDatabase.getInstance()
            var databaseReference: DatabaseReference = database.getReference("users")
            // getting the users current location
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
                == PackageManager.PERMISSION_GRANTED
            ) {
                map.isMyLocationEnabled = true
                getCurrentLocation()
            }

            // getting the id of the currently logged in user to check their account
            val userId = FirebaseAuth.getInstance().currentUser?.uid.toString()
            // getting the reference to the logged in user's stored favorites
            databaseReference = databaseReference.child(userId).child("favorites")
            databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        // stores coords of all user favorited books
                        val favCoords: MutableList<coords> = mutableListOf()
                        for (contactSnap in snapshot.children) {
                            // ensuring the user has at least 1 favorited book box
                            if (contactSnap.child("longitude").value != null && contactSnap.child("latitude").value != null && contactSnap.child("address") != null) {
                                // getting the long and lat coordinates of the boox boxes
                                val favlong: Double =
                                    (contactSnap.child("longitude").value as Double)
                                val favlat: Double = (contactSnap.child("latitude").value as Double)
                                val address: String = contactSnap.child("address").value as String
                                // and storing them as coords object to add to the list
                                val coord: coords = coords(favlat, favlong, address)
                                favCoords.add(coord)

                                // saves the coordinates of the closest bookbox and the current nearest distance
                                var minDistance: Double = 0.0
                                var closeLong: Double = 0.0
                                var closeLat: Double = 0.0
                                var boxName: String = ""
                                // comparing the user favourites with the book boxes
                                for ((lat, long, address) in favCoords) {
                                    var distance: Double = getDistance(lat, long)
                                    // if its the first or only fav then its set to be the closest box
                                    if (minDistance == 0.0) {
                                        minDistance = distance
                                        closeLong = long
                                        closeLat = lat
                                        boxName = address
                                    }
                                    // if the new book box had a closer distance then its now saved as such
                                    if (distance < minDistance) {
                                        closeLong = long
                                        closeLat = lat
                                        boxName = address
                                        minDistance = distance
                                    }
                                }
                                // converting to int to round then to string to display
                                val latLocation: String = closeLat.toInt().toString()
                                val longLocation: String = closeLong.toInt().toString()
                                // setting the location in the output text
                                // converting the coordinate values to a display message for the user and setting the text field to it
                                val outputText: TextView = view.findViewById(R.id.favLocation)
//                                val outString: String =
//                                    "Nearby BookBox at: (lat: $latLocation long: $longLocation)"
                                outputText.text = boxName
                            }

                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    // error reading from database
                }
            })

        }

        // Set up button to add a new book box
        view.findViewById<Button>(R.id.add_bookbox_button).setOnClickListener {
            // Start AddingBookBoxActivity to add a new book box
            startActivity(Intent(context, AddingBookBoxActivity::class.java))
        }
    }

    private fun navigateToBoxFragment() {
        parentFragmentManager.beginTransaction().apply {
            replace(
                R.id.fragment_container,
                BoxFragment()
            ) // Use the ID of your container where fragments are placed
            addToBackStack(null) // Add this transaction to the back stack
            commit() // Commit the transaction
        }
    }

    // calculates the distance from the user current location to the passed location
    private fun getDistance(lat: Double, long: Double): Double {
        var distance: Double
        var lat = lat
        var long = long
        // converting any negative values to positive for calculating the distance
        if (latitude < 0) {
            latitude *= -1
        }
        if (longitude < 0) {
            longitude *= -1
        }
        if (long < 0) {
            long *= -1
        }
        if (lat < 0) {
            lat *= -1
        }
        // setting the distance to be equal to the lateral distance between the two points
        if (lat < latitude) {
            distance = latitude - lat
        } else {
            distance = lat - latitude
        }
        // adds the longitude distance to the total distance variable
        if (long < longitude) {
            distance += longitude - long
        } else {
            distance += long - longitude
        }
        return distance
    }

}
