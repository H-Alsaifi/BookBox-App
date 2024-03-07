package ca.dal.cs.csci4176.group01undergraduate

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment

import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapsSdkInitializedCallback

class InteractiveMap : AppCompatActivity() {

    /**
     * Creates the activity
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // set content view
        setContentView(R.layout.activity_interactive_map)


        val map : Fragment = MapsFragment.newInstance()

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.mapContainer,map)
            .commit()


    }

}