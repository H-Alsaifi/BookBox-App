package ca.dal.cs.csci4176.group01undergraduate.view.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import ca.dal.cs.csci4176.group01undergraduate.model.ProfileState
import ca.dal.cs.csci4176.group01undergraduate.R
import ca.dal.cs.csci4176.group01undergraduate.view.activities.SignInActivity
import ca.dal.cs.csci4176.group01undergraduate.databinding.FragmentProfileBinding
import ca.dal.cs.csci4176.group01undergraduate.viewmodel.ProfileViewModel
import com.google.firebase.auth.FirebaseAuth

//fragment for user profile management, including editing profile information and handling account actions
class ProfileFragment : Fragment() {
    //ViewModel to manage the logic behind the UI
    private lateinit var viewModel: ProfileViewModel
    // Binding object to interact with the layout's views in a type-safe manner
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!//Getter for the non-null version of the binding object for easier access

    // ActivityResultLauncher for picking an image from the device, with a callback for the selected image URI
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        // If a URI is returned, update the profile picture in the ViewModel
        uri?.let { viewModel.updateProfilePicture(it) }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }
    // Set click listeners for each button, triggering dialogs and actions for profile management
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[ProfileViewModel::class.java]
        setupObservers()
        viewModel.loadUserProfile()//load user data every time

        //set click listeners for each button, triggering dialogs and actions for profile management
        binding.btnEditUsername.setOnClickListener {
            showEditUsernameDialog()
        }

        binding.btnEditPassword.setOnClickListener {
            showChangePasswordDialog()
        }

        binding.btnDeleteAccount.setOnClickListener {
            showDeleteAccountConfirmation()
        }

        binding.btnLogout.setOnClickListener {
            logoutUser()
        }

        binding.profileImage.setOnClickListener {
            // Launch the image picker
            imagePickerLauncher.launch("image/*")
        }
    }
    // Observes changes in the ViewModel's state and updates the UI accordingly
    private fun setupObservers() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            handleState(state)
        }
    }
    //displays a dialog for editing the user's username.
    private fun showEditUsernameDialog() {
        val input = EditText(requireContext())
        input.inputType = InputType.TYPE_CLASS_TEXT

        AlertDialog.Builder(requireContext()).apply {
            setTitle("Edit Username")
            setView(input)
            setPositiveButton("Save") { _, _ ->
                val newUsername = input.text.toString()
                // Update the username if the new value is not empty.
                if (newUsername.isNotEmpty()) {
                    viewModel.updateDisplayName(newUsername)
                } else {
                    Toast.makeText(requireContext(), "Username cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            setNegativeButton("Cancel", null)
        }.show()
    }

    //displays a dialog for changing the user's password
    private fun showChangePasswordDialog() {
        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(50, 0, 50, 0)
        }
        // EditText for the old password
        val oldPasswordInput = EditText(requireContext()).apply {
            hint = "Old Password"
            inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        // EditText for the new password.
        val newPasswordInput = EditText(requireContext()).apply {
            hint = "New Password"
            inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        //add both EditTexts to the layout.
        layout.addView(oldPasswordInput)
        layout.addView(newPasswordInput)

        AlertDialog.Builder(requireContext()).apply {
            setTitle("Change Password")
            setView(layout)
            setPositiveButton("Change") { _, _ ->
                val oldPassword = oldPasswordInput.text.toString()
                val newPassword = newPasswordInput.text.toString()
                //change the password if both fields are filled
                if (oldPassword.isNotEmpty() && newPassword.isNotEmpty()) {
                    viewModel.changePassword(oldPassword, newPassword)
                } else {
                    Toast.makeText(requireContext(), "All fields must be filled", Toast.LENGTH_SHORT).show()
                }
            }
            setNegativeButton("Cancel", null)
        }.show()
    }

    // Signs out the current user and navigates back to the sign-in screen.
    private fun logoutUser() {
        //use Firebase Auth to sign out the current user.
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(requireContext(), SignInActivity::class.java)
        // Clear all activities on the stack and start new with SignInActivity
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        // Start the SignInActivity
        startActivity(intent)
    }
    //displays a confirmation dialog before deleting the user's account
    private fun showDeleteAccountConfirmation() {
        AlertDialog.Builder(requireContext()).apply {
            setTitle("Delete Account")
            setMessage("Are you sure you want to permanently delete your account?")
            setPositiveButton("Delete") { _, _ ->
                // Call ViewModel to delete the account
                viewModel.deleteAccount()
            }
            setNegativeButton("Cancel", null)
        }.show()
    }
    //handles updates to the UI based on changes in the profile's state observed from the ViewModel
    @SuppressLint("SetTextI18n")
    private fun handleState(state: ProfileState) {
        when (state) {
            is ProfileState.DisplayNameUpdated -> {
                // Update display name UI component
                view?.findViewById<TextView>(R.id.tvUsername)?.text = state.username
            }
            is ProfileState.EmailUpdated -> {
                // Update email UI component
                view?.findViewById<TextView>(R.id.tvEmail)?.text = state.email
            }
            is ProfileState.ProfilePictureUpdated -> {
                // Update profile picture UI component
                view?.findViewById<ImageView>(R.id.profile_image)?.setImageURI(state.pictureUri)
            }
            is ProfileState.MembershipStatusUpdated -> {
                // Update membership status UI component
                view?.findViewById<TextView>(R.id.membership_status)?.text = "Membership Level: ${state.status}"
            }
            is ProfileState.PasswordChanged -> {
                // Notify user password was changed successfully
                Toast.makeText(requireContext(), "Password changed successfully", Toast.LENGTH_SHORT).show()
            }
            is ProfileState.AccountDeleted -> {
                // Notify user account was deleted and navigate to sign-in screen
                Toast.makeText(requireContext(), "Account deleted successfully", Toast.LENGTH_SHORT).show()
                logoutUser()
            }
            is ProfileState.Error -> {
                // Display error message
                Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
            }

        }
    }

    //cleans up the binding when the fragment's view is being destroyed to avoid memory leaks
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}

