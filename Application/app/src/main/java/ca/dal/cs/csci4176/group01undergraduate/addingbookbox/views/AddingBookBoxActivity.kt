package ca.dal.cs.csci4176.group01undergraduate.addingbookbox.views

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import ca.dal.cs.csci4176.group01undergraduate.MapsFragment
import ca.dal.cs.csci4176.group01undergraduate.R
import ca.dal.cs.csci4176.group01undergraduate.addingbookbox.intents.AddBookBoxIntent
import ca.dal.cs.csci4176.group01undergraduate.addingbookbox.models.AddBookBoxModel
import ca.dal.cs.csci4176.group01undergraduate.addingbookbox.viewmodels.AddBookBoxViewModel
import ca.dal.cs.csci4176.group01undergraduate.addingbookbox.viewmodels.AddBookBoxViewModelFactory
import ca.dal.cs.csci4176.group01undergraduate.databinding.AddingBookBoxActivityBinding
import ca.dal.cs.csci4176.group01undergraduate.displayingbookbox.BoxFragment

class AddingBookBoxActivity : AppCompatActivity() {

    private lateinit var binding: AddingBookBoxActivityBinding
    private lateinit var viewModel: AddBookBoxViewModel
    private var pictureUri: Uri? = null

    // This launcher will handle the result from the image picker
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            pictureUri = result.data?.data
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AddingBookBoxActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val factory = AddBookBoxViewModelFactory(AddBookBoxModel())
        viewModel = ViewModelProvider(this, factory).get(AddBookBoxViewModel::class.java)

        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        binding.apply {
            fabUpload.setOnClickListener {
                // Invoke the image picker
                pickImageFromGallery()
            }
            confirmButton.setOnClickListener {
                val name = nameEditText.text.toString()
                val location = locationEditText.text.toString()
                val description = descriptionEditText.text.toString()

                if (name.isEmpty() || location.isEmpty() || description.isEmpty() || pictureUri == null) {
                    Toast.makeText(this@AddingBookBoxActivity, "All fields and picture are required.", Toast.LENGTH_LONG).show()
                } else {
                    viewModel.processIntent(
                        AddBookBoxIntent.SubmitDetails(name, location, description, pictureUri!!)
                    )
                }
            }
            mapButton.setOnClickListener {
                // Logic to navigate to the map
                navigateToMap()
            }
        }
    }

    private fun navigateToMap() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, MapsFragment())
            .addToBackStack(null)
            .commit()
    }



    private fun navigateToBoxFragment() {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragment_container, BoxFragment())
            addToBackStack(null)
            commit()
        }
    }


    private fun observeViewModel() {
        viewModel.state.observe(this) { state ->
            if (state.isSuccessful) {
                Toast.makeText(this, "Book Box added successfully", Toast.LENGTH_SHORT).show()
                navigateToBoxFragment()
            } else if (state.error != null) {
                // Handle error state
                Toast.makeText(this, "Error: ${state.error.message}", Toast.LENGTH_LONG).show()
            }

        }
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
        }
        pickImageLauncher.launch(intent)
    }



}
