package ca.dal.cs.csci4176.group01undergraduate.technicalSupport

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import ca.dal.cs.csci4176.group01undergraduate.R

// Defines a fragment class for the contact form UI
class ContactFormFragment : Fragment() {

    // Lateinit variables for UI elements, will be initialized in onCreateView
    private lateinit var editTextName: EditText
    private lateinit var editTextEmail: EditText
    private lateinit var editTextMessage: EditText
    private lateinit var buttonSubmit: Button

    // ViewModel for managing UI-related data in a lifecycle-conscious way, shared across the activity
    private val supportViewModel: SupportViewModel by activityViewModels { SupportViewModelFactory() }

    // Inflates the fragment's view and initializes UI components when the view is created
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the XML layout for this fragment
        val view = inflater.inflate(R.layout.fragment_contact_form, container, false)

        // Find and assign references to the UI elements within the inflated layout
        editTextName = view.findViewById(R.id.editTextName)
        editTextEmail = view.findViewById(R.id.editTextEmail)
        editTextMessage = view.findViewById(R.id.editTextMessage)
        buttonSubmit = view.findViewById(R.id.buttonSubmit)

        // Assign a listener to the submit button to trigger form submission
        buttonSubmit.setOnClickListener {
            submitForm()
        }

        return view
    }

    // Sets up observers on the ViewModel to react to submission results after the view is created
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Observe submission results from the ViewModel to provide user feedback
        supportViewModel.submissionResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is SubmissionResult.Success -> {
                    // Display a success message to the user
                    Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()

                    // Return to the previous menu, indicating completion
                    navigateBackToMenu()
                }
                is SubmissionResult.Error -> {
                    // Display an error message to the user
                    Toast.makeText(context, result.error, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // Handles navigation back to the main menu or previous fragment
    private fun navigateBackToMenu() {
        // Ensure the fragment is attached before attempting to pop the back stack
        if (isAdded) {
            parentFragmentManager.popBackStack()
        }
    }

    // Validates and submits the contact form data
    private fun submitForm() {
        // Extract and trim input from text fields
        val name = editTextName.text.toString().trim()
        val email = editTextEmail.text.toString().trim()
        val message = editTextMessage.text.toString().trim()

        // Validate that none of the fields are empty, providing feedback if they are
        if (name.isEmpty() || email.isEmpty() || message.isEmpty()) {
            Toast.makeText(context, "All fields are required.", Toast.LENGTH_SHORT).show()
            return
        }

        // Validate the email format, providing feedback if invalid
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(context, "Please enter a valid email address.", Toast.LENGTH_SHORT).show()
            return
        }

        // Submit the validated data through the ViewModel
        supportViewModel.submitSupportRequest(name, email, message)
    }
}
