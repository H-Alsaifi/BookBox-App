package ca.dal.cs.csci4176.group01undergraduate.view.fragments

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import ca.dal.cs.csci4176.group01undergraduate.R

/**
 * A simple [Fragment] subclass.
 * Use the [MapErrorFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MapErrorFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                parentFragmentManager
                    .beginTransaction()
                    .replace(R.id.fragment_container, MapsFragment())
                    .commit()
            }
        }

    private fun requestLocationPermission() {
        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_map_error, container, false)

        // text view
        view.findViewById<TextView>(R.id.mapErrorMessage).visibility = View.VISIBLE

        // button
        view.findViewById<Button>(R.id.enableLocation).setOnClickListener {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.fromParts("package", "ca.dal.cs.csci4176.group01undergraduate", null)
            startActivity(intent)
        }



        return view
    }

    override fun onResume() {
        super.onResume()
        requestLocationPermission()
    }
}