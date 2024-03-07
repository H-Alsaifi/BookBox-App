package ca.dal.cs.csci4176.group01undergraduate

import android.annotation.SuppressLint
import android.graphics.Color
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.compose.material3.BottomSheetDefaults
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMapLoadedCallback
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener

import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.AdvancedMarker
import com.google.android.gms.maps.model.AdvancedMarkerOptions
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PinConfig
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog

private const val PLACES = "places"
class MapsFragment : Fragment(), OnMarkerClickListener{

    // Halifax location
    private val northEast: LatLng = LatLng(44.684204, -63.543474)
    private val southWest: LatLng = LatLng(44.6209409, -63.629210)
    private var places: ArrayList<LatLng> = ArrayList()

    /**
     * Acts as a callback
     */
    private val callback = OnMapReadyCallback{
        addMarkers(it)
        it.setOnMarkerClickListener(this)

    }

    override fun onMarkerClick(marker: Marker):Boolean {

        if(activity is InteractiveMap){
            val interactiveMap = activity as InteractiveMap
            interactiveMap
                .findViewById<LinearLayout>(R.id.bottomSheetLayout)
                .findViewById<TextView>(R.id.coordinates)
                .text = marker.position.toString()


            interactiveMap.bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
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
        val view = inflater.inflate(R.layout.fragment_maps, container, false)



        return view
    }

    /**
     * Creates the maps itself
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
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
    }

    companion object{

        /**
         * Create a new instance of [MapsFragment] using the parameters (so far non for now)
         */
        @JvmStatic
        fun newInstance() =
            MapsFragment().apply {
            arguments = Bundle().apply {
            }
        }
    }
}