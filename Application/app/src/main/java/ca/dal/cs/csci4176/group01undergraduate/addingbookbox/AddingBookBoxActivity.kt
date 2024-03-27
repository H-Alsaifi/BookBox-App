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
import android.util.Log
import androidx.fragment.app.FragmentManager
import ca.dal.cs.csci4176.group01undergraduate.addingbookbox.BookBoxViewModel
import ca.dal.cs.csci4176.group01undergraduate.addingbookbox.viewmodels.BookBoxViewModelFactory
import ca.dal.cs.csci4176.group01undergraduate.addingbookbox.BookBoxRepository
import androidx.lifecycle.lifecycleScope
import ca.dal.cs.csci4176.group01undergraduate.MainActivity
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch


/**
 * Activity for adding a new book box. Handles user input, image selection,
 * and submitting new book box details to Firebase.
 */
class AddingBookBoxActivity : AppCompatActivity() {

    // Binding instance for accessing the layout views
    private lateinit var binding: AddingBookBoxActivityBinding
    // URI of the image picked by the user
    private var pickedImageUri: Uri? = null

    // ViewModel instance for managing UI-related data and operations
    private val viewModel: BookBoxViewModel by viewModels {
        // ViewModel factory to inject dependencies into the ViewModel
        BookBoxViewModelFactory(BookBoxRepository(applicationContext)) {
            // Lambda to check location permission status
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    // Permission launcher for requesting location permissions
    private val requestLocationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // Handle the granted permission case
                fetchLocation()
            } else {
                // Notify the user when permission is denied
                Toast.makeText(this, "Location permission is required to use this feature.", Toast.LENGTH_SHORT).show()
            }
        }

    // Launcher for picking an image from the gallery
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            // Store the picked image URI and notify the ViewModel
            pickedImageUri = it
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

    /**
     * Setup UI elements and event listeners.
     */
    private fun setupUI() {
        binding.apply {
            fabUpload.setOnClickListener {
                // Trigger image selection from the gallery
                pickImageFromGallery()
            }
            confirmButton.setOnClickListener {
                val description = descriptionEditText.text.toString().trim()
                if (pickedImageUri == null) {
                    Toast.makeText(this@AddingBookBoxActivity, "Image is required.", Toast.LENGTH_LONG).show()
                } else {
                    // Submit book box details when an image has been selected
                    viewModel.offerIntent(BookBoxIntent.SubmitDetails(description.ifEmpty { "No Description" }, pickedImageUri.toString()))
                }
            }

            getLocationButton.setOnClickListener {
                // Request location update
                if (!hasLocationPermission()) {
                    requestLocationPermission()
                } else {
                    viewModel.offerIntent(BookBoxIntent.FetchCurrentLocation)
                }
            }
            mapButton.setOnClickListener {
                // Navigate to the map view
                navigateToMap()
            }
        }
    }

    /**
     * Launch an intent to pick an image from the gallery.
     */
    private fun pickImageFromGallery() {
        pickImageLauncher.launch("image/*")
    }

    /**
     * Request the current location if permissions are granted.
     */
    private fun fetchLocation() {
        if (hasLocationPermission()) {
            viewModel.offerIntent(BookBoxIntent.FetchCurrentLocation)
        } else {
            requestLocationPermission()
        }
    }

    /**
     * Check if location permissions are granted.
     * @return Boolean indicating whether location permissions are granted.
     */
    private fun hasLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Navigate to the main activity which may contain the map fragment.
     */
    private fun navigateToMap() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    // Constants used within the activity, for example, request codes for permissions
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    // Continuation of requesting fine location permission using the permission launcher
    private fun requestLocationPermission() {
        requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    /**
     * Handles the result of the permission request initiated by [requestLocationPermission].
     * If permissions are granted, proceeds with fetching the location. Otherwise, informs the user.
     *
     * @param requestCode The integer request code originally supplied to requestPermissions(),
     * allowing you to identify who this result came from.
     * @param permissions The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     * which is either PERMISSION_GRANTED or PERMISSION_DENIED. Never null.
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED })) {
                // Proceed with the location-related operation because all required permissions have been granted by the user
                fetchLocation()
            } else {
                // Inform the user that location permission is essential for this feature to work
                Toast.makeText(this, "You need to grant location permissions to use this feature.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Navigate to the BoxFragment which displays the added book box details.
     * Utilizes the SupportFragmentManager to replace the current view with the BoxFragment.
     */
    private fun navigateToBoxFragment() {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragment_container, BoxFragment())
            addToBackStack(null) // Allows users to go back to the previous fragment/state
            commit() // Commit the transaction to apply changes
        }
    }

    /**
     * Observes changes in the ViewModel's state and updates the UI accordingly.
     * Shows location information, displays success messages, and handles errors.
     */
    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.state.collect { state ->
                state.location?.let {
                    // Update UI with location details when available
                    val locationText = "Lat: ${it.latitude}, Lon: ${it.longitude}"
                    binding.locationDisplay.text = locationText
                }

                if (state.isSuccessful && !state.isLoading && state.error == null) {
                    // Show success message and navigate to map view upon successful addition of a book box
                    Toast.makeText(this@AddingBookBoxActivity, "Book Box added successfully", Toast.LENGTH_SHORT).show()
                    navigateToMap()
                } else if (state.error != null) {
                    // Handle and display errors if any occurred during the process
                    handleError(state.error)
                }
            }
        }
    }

    /**
     * Handles displaying an error message to the user in case of an exception.
     * @param error The exception that occurred during the book box addition process.
     */
    private fun handleError(error: Exception) {
        // Display the error message to the user
        Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_LONG).show()
    }


}
