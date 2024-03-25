package ca.dal.cs.csci4176.group01undergraduate

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
import ca.dal.cs.csci4176.group01undergraduate.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth

class ProfileFragment : Fragment() {
    private lateinit var viewModel: ProfileViewModel
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!


    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.updateProfilePicture(it) }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[ProfileViewModel::class.java]
        setupObservers()

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

    private fun setupObservers() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            handleState(state)
        }
    }

    private fun showEditUsernameDialog() {
        val input = EditText(requireContext())
        input.inputType = InputType.TYPE_CLASS_TEXT

        AlertDialog.Builder(requireContext()).apply {
            setTitle("Edit Username")
            setView(input)
            setPositiveButton("Save") { dialog, which ->
                val newUsername = input.text.toString()
                if (newUsername.isNotEmpty()) {
                    viewModel.updateDisplayName(newUsername)
                } else {
                    Toast.makeText(requireContext(), "Username cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            setNegativeButton("Cancel", null)
        }.show()
    }

    private fun showChangePasswordDialog() {
        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(50, 0, 50, 0)
        }

        val oldPasswordInput = EditText(requireContext()).apply {
            hint = "Old Password"
            inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD
        }

        val newPasswordInput = EditText(requireContext()).apply {
            hint = "New Password"
            inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD
        }

        layout.addView(oldPasswordInput)
        layout.addView(newPasswordInput)

        AlertDialog.Builder(requireContext()).apply {
            setTitle("Change Password")
            setView(layout)
            setPositiveButton("Change") { dialog, which ->
                val oldPassword = oldPasswordInput.text.toString()
                val newPassword = newPasswordInput.text.toString()
                if (oldPassword.isNotEmpty() && newPassword.isNotEmpty()) {
                    viewModel.changePassword(oldPassword, newPassword)
                } else {
                    Toast.makeText(requireContext(), "All fields must be filled", Toast.LENGTH_SHORT).show()
                }
            }
            setNegativeButton("Cancel", null)
        }.show()
    }


    private fun logoutUser() {
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(requireContext(), SignIn::class.java)
        // Clear all activities on the stack and start new with SignInActivity
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        // Start the SignInActivity
        startActivity(intent)
    }

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
                logoutUser() // Assuming logoutUser navigates to SignInFragment/Activity
            }
            is ProfileState.Error -> {
                // Display error message
                Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
            }
            // Add additional states as needed
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}

