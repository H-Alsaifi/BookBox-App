package ca.dal.cs.csci4176.group01undergraduate

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
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
import com.google.maps.DirectionsApi
import com.google.maps.GeoApiContext
import com.google.maps.PendingResult
import com.google.maps.internal.PolylineEncoding
import com.google.maps.model.DirectionsResult
import com.google.maps.model.TravelMode

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
        places.forEach { place->
            map.addMarker(
                MarkerOptions()
                    .position(place)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_book_box))
            )
        }
    }

    /**
     * If [MapsFragment] successfully created assign some values
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



        // dummy text
        places.add(LatLng(44.6375, -63.59075))
        places.add(LatLng(44.6496389, -63.5716944))
        places.add(LatLng(44.6465278, -63.5943611))
        places.add(LatLng(44.63189566264618, -63.581212724391236))
        places.add(LatLng(44.65878323733942, -63.60420573442021))
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
                it.moveCamera(CameraUpdateFactory.newLatLngBounds(halifaxBounds,10))
            }
        }
        mapFragment?.getMapAsync(callback)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())


    }

}