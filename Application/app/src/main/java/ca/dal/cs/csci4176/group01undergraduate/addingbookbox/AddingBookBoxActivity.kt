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
import ca.dal.cs.csci4176.group01undergraduate.addingbookbox.BookBoxViewModel
import ca.dal.cs.csci4176.group01undergraduate.addingbookbox.viewmodels.BookBoxViewModelFactory
import ca.dal.cs.csci4176.group01undergraduate.addingbookbox.BookBoxRepository
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch


class AddingBookBoxActivity : AppCompatActivity() {

    private lateinit var binding: AddingBookBoxActivityBinding

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

    // Initialize the picture picker launcher
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) {
            pictureUri: Uri? ->
        pictureUri?.let {
            // Here, you might want to update your ViewModel state to reflect the selected image URI
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
                // Assuming pictureUri is handled within the ViewModel after image selection
                // This checks for name and description only as the picture URI handling is updated
                if (name.isEmpty() || description.isEmpty()) {
                    Toast.makeText(this@AddingBookBoxActivity, "Name, description, and picture are required.", Toast.LENGTH_LONG).show()
                } else {
                    // Dispatch SubmitDetails intent directly with name, description, and a placeholder for pictureUri
                    // The actual picture URI should be handled by observing ViewModel state changes when the picture is selected
                    viewModel.offerIntent(BookBoxIntent.SubmitDetails(name, description, "placeholderForActualPictureUri"))
                }
            }
            getLocationButton.setOnClickListener {
                // Dispatch FetchCurrentLocation intent
                viewModel.offerIntent(BookBoxIntent.FetchCurrentLocation)
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
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, MapsFragment())
            .addToBackStack(null)
            .commit()
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
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
        // Use lifecycleScope to collect from StateFlow
        lifecycleScope.launch {
            viewModel.state.collect { state ->
                // Update the UI based on the state object
                if (state.isLoading) {
                    // Show loading indicator
                } else {
                    if (state.isSuccessful) {
                        // Hide loading indicator and navigate to the success fragment
                        Toast.makeText(this@AddingBookBoxActivity, "Book Box added successfully", Toast.LENGTH_SHORT).show()
                        navigateToBoxFragment()
                    }
                    // Handle error state
                    state.error?.let { error ->
                        Toast.makeText(this@AddingBookBoxActivity, "Error: ${error.message}", Toast.LENGTH_LONG).show()
                    }
                    // Update location in the UI if needed
                    state.location?.let { location ->
                        // Use location to update the UI
                    }
                    // Update image URL in the UI if needed
                    state.imageUrl?.let { imageUrl ->
                        // Use imageUrl to update the UI
                    }
                }
            }
        }
    }





}
