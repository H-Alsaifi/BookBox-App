package ca.dal.cs.csci4176.group01undergraduate.addingbookbox

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import ca.dal.cs.csci4176.group01undergraduate.MapsFragment
import ca.dal.cs.csci4176.group01undergraduate.R
import ca.dal.cs.csci4176.group01undergraduate.databinding.AddingBookBoxActivityBinding
import ca.dal.cs.csci4176.group01undergraduate.displayingbookbox.BoxFragment
import android.Manifest
import android.graphics.PixelFormat
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentManager
import ca.dal.cs.csci4176.group01undergraduate.addingbookbox.BookBoxViewModel
import ca.dal.cs.csci4176.group01undergraduate.addingbookbox.viewmodels.BookBoxViewModelFactory
import ca.dal.cs.csci4176.group01undergraduate.addingbookbox.BookBoxRepository
import androidx.lifecycle.lifecycleScope
import ca.dal.cs.csci4176.group01undergraduate.MainActivity
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch


class AddingBookBoxActivity : AppCompatActivity() {

    private lateinit var binding: AddingBookBoxActivityBinding
    private var pickedImageUri: Uri? = null

    private val viewModel: BookBoxViewModel by viewModels {
        // Provide the lambda when creating the ViewModelFactory
        BookBoxViewModelFactory(BookBoxRepository(applicationContext)) {
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    // Define a permission launcher for location permission request
    private val requestLocationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // Permission was granted, proceed with fetching location
                fetchLocation()
            } else {
                // Permission was denied, show an explanatory toast or dialog
                Toast.makeText(this, "Location permission is required to use this feature.", Toast.LENGTH_SHORT).show()
            }
        }


    // Initialize the picture picker launcher
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            pickedImageUri = it // Store the picked image URI
            viewModel.offerIntent(BookBoxIntent.UploadPicture(it.toString()))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AddingBookBoxActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        binding.apply {
            fabUpload.setOnClickListener {
                pickImageFromGallery()
            }
            confirmButton.setOnClickListener {
                val name = nameEditText.text.toString()
                val description = descriptionEditText.text.toString()
                if (name.isEmpty() || description.isEmpty() || pickedImageUri == null) {
                    Toast.makeText(this@AddingBookBoxActivity, "Name, description, and picture are required.", Toast.LENGTH_LONG).show()
                } else {
                    // Use the actual picked image URI here
                    viewModel.offerIntent(BookBoxIntent.SubmitDetails(name, description, pickedImageUri.toString()))
                }
            }
            binding.getLocationButton.setOnClickListener {
                // Dispatch FetchCurrentLocation intent and observe the change
                if (!hasLocationPermission()) {
                    requestLocationPermission()
                } else {
                    viewModel.offerIntent(BookBoxIntent.FetchCurrentLocation)
                }
            }
            mapButton.setOnClickListener {
                navigateToMap()
            }
        }
    }

    private fun pickImageFromGallery() {
        // Launch an intent to pick an image, which will be handled by pickImageLauncher
        pickImageLauncher.launch("image/*")
    }

    private fun fetchLocation() {
        if (hasLocationPermission()) {
            // Fetch the location
            viewModel.offerIntent(BookBoxIntent.FetchCurrentLocation)
        } else {
            // Request permissions from the user
            requestLocationPermission()
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun navigateToMap() {
        Log.d("AddingBookBoxActivity", "Navigating to MapsFragment")
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

//    private fun requestLocationPermission() {
//        ActivityCompat.requestPermissions(
//            this,
//            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
//            LOCATION_PERMISSION_REQUEST_CODE
//        )
//    }
    private fun requestLocationPermission() {
        // Request fine location permission using the permission launcher
        requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED })) {
                // All required permissions have been granted, carry on
                fetchLocation()
            } else {
                // Permissions were denied or request was cancelled
                Toast.makeText(this, "You need to grant location permissions to use this feature.", Toast.LENGTH_SHORT).show()
            }
        }
    }





    private fun navigateToBoxFragment() {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragment_container, BoxFragment())
            addToBackStack(null)
            commit()
        }
    }


    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.state.collect { state ->
                // Update the location display when location data is available
                state.location?.let {
                    val locationText = "Lat: ${it.latitude}, Lon: ${it.longitude}"
                    binding.locationDisplay.text = locationText
                }

                if (state.isSuccessful && !state.isLoading && state.error == null) {
                   displayMessage("Book Box added successfully")
//                    Toast.makeText(this@AddingBookBoxActivity, "Book Box added successfully", Toast.LENGTH_SHORT).show()
                    navigateToMap() // This ensures navigation occurs after success
                } else if (state.error != null) {
                    handleError(state.error)
                }
            }
        }
    }

    private fun handleError(error: Exception) {
        // Handle the error
        Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_LONG).show()
    }

    private fun displayMessage(message: String) {
         val builder = AlertDialog.Builder(this)
         builder.setMessage(message)
         builder.setTitle("Update !")
         builder.setCancelable(false)
         val alertDialog = builder.create()
         alertDialog.show()
        Handler(Looper.getMainLooper()).postDelayed({
           alertDialog.dismiss()
          }, 5000)

        }
}
