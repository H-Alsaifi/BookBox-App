package ca.dal.cs.csci4176.group01undergraduate

import androidx.fragment.app.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap.OnMapLoadedCallback

import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds

private const val MAPS_CALLBACK = "callback"
class MapsFragment : Fragment() {

    // Halifax location
    private val northEast: LatLng = LatLng(44.684204, -63.543474)
    private val southWest: LatLng = LatLng(44.6209409, -63.629210)

    /**
     * Acts as a callback
     *
     */
    // TODO: Set the pins for the books
    private val callback = OnMapReadyCallback{


    }

    private val callback2 = OnMapLoadedCallback{


    }

    /**
     * If [MapsFragment] successfully created assign some values
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    /**
     * Inflates the map
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_maps, container, false)
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