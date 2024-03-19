package ca.dal.cs.csci4176.group01undergraduate.addingbookbox.views

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import ca.dal.cs.csci4176.group01undergraduate.addingbookbox.intents.AddBookBoxIntent
import ca.dal.cs.csci4176.group01undergraduate.addingbookbox.models.AddBookBoxModel
import ca.dal.cs.csci4176.group01undergraduate.addingbookbox.viewmodels.AddBookBoxViewModel
import ca.dal.cs.csci4176.group01undergraduate.addingbookbox.viewmodels.AddBookBoxViewModelFactory
import ca.dal.cs.csci4176.group01undergraduate.databinding.AddingBookBoxActivityBinding

class AddingBookBoxActivity : AppCompatActivity() {

    private lateinit var binding: AddingBookBoxActivityBinding
    private lateinit var viewModel: AddBookBoxViewModel
    private var pictureUri: Uri? = null

    // This launcher will handle the result from the image picker
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            pictureUri = result.data?.data
            // Here you might want to update your UI to show the selected image
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AddingBookBoxActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Assuming AddBookBoxModel is constructed somewhere in your code.
        // You need to have an instance of AddBookBoxModel to pass into your ViewModelFactory.
        val factory = AddBookBoxViewModelFactory(AddBookBoxModel())
        viewModel = ViewModelProvider(this, factory).get(AddBookBoxViewModel::class.java)

        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        binding.apply {
            uploadButton.setOnClickListener {
                // Invoke the image picker
                pickImageFromGallery()
            }
            confirmButton.setOnClickListener {
                val name = nameEditText.text.toString()
                val location = locationEditText.text.toString()
                val description = descriptionEditText.text.toString()

                if (name.isEmpty() || location.isEmpty() || description.isEmpty() || pictureUri == null) {
                    Toast.makeText(this@AddingBookBoxActivity, "All fields and picture are required.", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }

                viewModel.processIntent(
                    AddBookBoxIntent.SubmitDetails(name, location, description, pictureUri!!)
                )
            }
        }
    }

    private fun observeViewModel() {
        viewModel.state.observe(this) { state ->
            if (state.isSuccessful) {
                // Assuming you want to navigate back to the SearchFragment.
                Toast.makeText(this, "Book Box added successfully", Toast.LENGTH_SHORT).show()
                finish() // Ends the current activity and takes you back to the previous one in the stack.
            } else if (state.error != null) {
                // Handle error state
                Toast.makeText(this, "Error: ${state.error.message}", Toast.LENGTH_LONG).show()
            }

        }
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        pickImageLauncher.launch(intent)
    }
}
