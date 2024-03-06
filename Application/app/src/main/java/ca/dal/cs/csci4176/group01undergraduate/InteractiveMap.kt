package ca.dal.cs.csci4176.group01undergraduate

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import ca.dal.cs.csci4176.group01undergraduate.databinding.ActivityInteractiveMapBinding

class InteractiveMap : AppCompatActivity() {

    /**
     * Creates the activity
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_interactive_map)

        val map : Fragment = MapsFragment.newInstance()

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.mapContainer,map)
            .commit()
    }


}