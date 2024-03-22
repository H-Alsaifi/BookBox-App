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

class ContactFormFragment : Fragment() {

    private lateinit var editTextName: EditText
    private lateinit var editTextEmail: EditText
    private lateinit var editTextMessage: EditText
    private lateinit var buttonSubmit: Button

    // Use the 'activityViewModels' delegate to share the ViewModel across the entire activity
    private val supportViewModel: SupportViewModel by activityViewModels { SupportViewModelFactory() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_contact_form, container, false)

        // Bind the input fields and button
        editTextName = view.findViewById(R.id.editTextName)
        editTextEmail = view.findViewById(R.id.editTextEmail)
        editTextMessage = view.findViewById(R.id.editTextMessage)
        buttonSubmit = view.findViewById(R.id.buttonSubmit)

        // Set up a click listener for the submit button
        buttonSubmit.setOnClickListener {
            submitForm()
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        supportViewModel.submissionResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is SubmissionResult.Success -> {
                    // Handle success
                    Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()

                    // Navigate back to the MenuFragment
                    navigateBackToMenu()
                }
                is SubmissionResult.Error -> {
                    // Handle error
                    Toast.makeText(context, result.error, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun navigateBackToMenu() {
        // Check if the fragment is added to an activity and if so, pop the back stack
        if (isAdded) {
            parentFragmentManager.popBackStack()
        }
    }


    private fun submitForm() {
        val name = editTextName.text.toString().trim()
        val email = editTextEmail.text.toString().trim()
        val message = editTextMessage.text.toString().trim()

        // Check if any of the fields are empty
        if (name.isEmpty() || email.isEmpty() || message.isEmpty()) {
            Toast.makeText(context, "All fields are required.", Toast.LENGTH_SHORT).show()
            return
        }

        // Validate the email with a basic pattern match
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(context, "Please enter a valid email address.", Toast.LENGTH_SHORT).show()
            return
        }

        // Use the ViewModel to submit the request
        supportViewModel.submitSupportRequest(name, email, message)
    }
}
