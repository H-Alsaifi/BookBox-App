package ca.dal.cs.csci4176.group01undergraduate

import android.content.Intent
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import ca.dal.cs.csci4176.group01undergraduate.addingbookbox.AddingBookBoxActivity
import ca.dal.cs.csci4176.group01undergraduate.displayingbookbox.BoxFragment
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
import com.google.android.material.bottomsheet.BottomSheetBehavior

class MapsFragment : Fragment(), OnMarkerClickListener{

    // Halifax location
    private val northEast: LatLng = LatLng(44.684204, -63.543474)
    private val southWest: LatLng = LatLng(44.6209409, -63.629210)
    private var places: ArrayList<LatLng> = ArrayList()

    // maps
    lateinit var bottomSheetBehavior : BottomSheetBehavior<LinearLayout>
    private lateinit var bottomSheetLayout: LinearLayout

    /**
     * Acts as a callback
     */
    private val callback = OnMapReadyCallback{


        addMarkers(it)
        it.setOnMarkerClickListener(this)
        it.uiSettings.isZoomControlsEnabled = true

    }

    override fun onMarkerClick(marker: Marker):Boolean {

        if(activity is MainActivity){
            val interactiveMap = activity as MainActivity
            interactiveMap
                .findViewById<LinearLayout>(R.id.bottomSheetLayout)
                .findViewById<TextView>(R.id.coordinates)
                .text = marker.position.toString()


            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }

        return true
    }


    /**
     * Add pins to map
     * icon taken from: https://www.figma.com/file/62O8YMjZOLkkTe9jqkmp61/coolicons-%7C-Free-Iconset-(Community)?type=design&t=Umarm5N5x9E9bXGJ-6
     */
    private fun addMarkers(map: GoogleMap){
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
//        arguments?.let {
//            places = it.getParcelableArrayList(PLACES)
//        }

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

        // bottom sheet
        bottomSheetLayout = view.findViewById(R.id.bottomSheetLayout)
        bottomSheetBehavior = BottomSheetBehavior
            .from(bottomSheetLayout)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

        return view
    }

    /**
     * Creates the maps itself
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up map fragment
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
        mapFragment?.getMapAsync {
            it.setOnMapLoadedCallback {
                val halifaxBounds = LatLngBounds.builder()
                    .include(northEast)
                    .include(southWest)
                    .build()
                it.moveCamera(CameraUpdateFactory.newLatLngBounds(halifaxBounds, 10))
            }
        }

        // Set up button to view the list of book boxes
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
        activity?.supportFragmentManager?.beginTransaction()?.apply {
            replace(R.id.fragment_container, BoxFragment()) // Use the ID of your container where fragments are placed
            addToBackStack(null) // Add this transaction to the back stack
            commit() // Commit the transaction
        }
    }


}