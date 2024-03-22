package ca.dal.cs.csci4176.group01undergraduate

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth

class ProfileActivity : AppCompatActivity() {

    private lateinit var viewModel: ProfileViewModel
    private lateinit var profileImageView: ImageView

    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            viewModel.updateProfilePicture(it)
            profileImageView.setImageURI(it)
        }
    }

    @SuppressLint("MissingInflatedId", "WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Initialize the ViewModel
        viewModel = ViewModelProvider(this)[ProfileViewModel::class.java]

        viewModel.loadUserProfile()

        val nameTextView: TextView = findViewById(R.id.tvUsername)
        val EditPassword: ImageView = findViewById(R.id.btnEditPassword)
        val deleteAccountButton: Button = findViewById(R.id.btnDeleteAccount)
        val logoutButton: Button = findViewById(R.id.btnLogout)
        profileImageView = findViewById(R.id.profile_image)

        val oldPasswordEditText = findViewById<EditText>(R.id.oldPassword)
        val newPasswordEditText = findViewById<EditText>(R.id.newPassword)
        val changePasswordButton = findViewById<Button>(R.id.changePasswordButton)

        findViewById<ImageView>(R.id.btnEditUsername).setOnClickListener {
            showEditDialog("Edit Name", nameTextView.text.toString()) { newName ->
                nameTextView.text = newName
                viewModel.updateDisplayName(newName)
            }
        }

        profileImageView.setOnClickListener {
            openImageSelector()
        }

        deleteAccountButton.setOnClickListener {
            showDeleteAccountConfirmation()
        }

        logoutButton.setOnClickListener {
            logoutUser()
        }
        EditPassword.setOnClickListener {
            showChangePasswordDialog()
        }

        changePasswordButton.setOnClickListener {
            val oldPassword = oldPasswordEditText.text.toString().trim()
            val newPassword = newPasswordEditText.text.toString().trim()

            if (oldPassword.isEmpty() || newPassword.isEmpty()) {
                Toast.makeText(this, "Both fields are required", Toast.LENGTH_SHORT).show()
            } else {
                // Assuming your ViewModel's changePassword function takes two Strings for old and new passwords
                viewModel.changePassword(oldPassword, newPassword)
            }
        }

        // Observing ViewModel's state LiveData to update the UI accordingly
        viewModel.state.observe(this) { state ->
            handleState(state)
        }
    }

    private fun openImageSelector() {
        imagePickerLauncher.launch("image/*")
    }

    private fun logoutUser() {

        FirebaseAuth.getInstance().signOut()
        // Navigate back to SignInActivity
        val intent = Intent(this, SignIn::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        Toast.makeText(this, "You have logged out", Toast.LENGTH_SHORT).show()
        finish() // Ensure this activity is cleared from the back stack
    }

    private fun showDeleteAccountConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Delete Account")
            .setMessage("Are you sure you want to delete your account? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteAccount()
                navigateToMainActivityWithToast("Account deleted")
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    private fun showChangePasswordDialog() {
        val oldPasswordInput = EditText(this)
        oldPasswordInput.hint = "Old Password"
        val newPasswordInput = EditText(this)
        newPasswordInput.hint = "New Password"

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.addView(oldPasswordInput)
        layout.addView(newPasswordInput)

        AlertDialog.Builder(this)
            .setTitle("Change Password")
            .setView(layout)
            .setPositiveButton("Change") { _, _ ->
                val oldPassword = oldPasswordInput.text.toString().trim()
                val newPassword = newPasswordInput.text.toString().trim()
                if (oldPassword.isNotEmpty() && newPassword.isNotEmpty()) {
                    viewModel.changePassword(oldPassword, newPassword)
                } else {
                    Toast.makeText(this, "Both fields are required.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }


    private fun navigateToMainActivityWithToast(message: String) {
        val intent = Intent(this, SignIn::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("toast_message", message)
        }
        startActivity(intent)
        finish()
    }

    private fun showEditDialog(field: String, currentValue: String, onSave: (String) -> Unit) {
        val input = EditText(this).apply {
            setText(currentValue)
        }
        AlertDialog.Builder(this)
            .setTitle(field)
            .setView(input)
            .setPositiveButton("Save") { dialog, _ ->
                onSave(input.text.toString())
                dialog.dismiss()
                //Toast.makeText(this, "Username updated successfully", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }
            .show()
    }
    private fun navigateToSignInWithToast(message: String) {
        val intent = Intent(this, SignIn::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
        finish()
    }
    private fun updateMembershipStatusUI(status: String) {
        findViewById<TextView>(R.id.membership_status).text = "Membership Level: $status"
    }

    private fun handleState(state: ProfileState) {
        when (state) {
            is ProfileState.MembershipStatusUpdated -> updateMembershipStatusUI(state.status)
            is ProfileState.EmailUpdated -> {
                // Update the TextView with the new email
                val emailTextView: TextView = findViewById(R.id.tvEmail)
                emailTextView.text = state.email
                // Show a toast message
                Toast.makeText(this, "Email updated successfully", Toast.LENGTH_SHORT).show()
            }
            is ProfileState.DisplayNameUpdated -> {
                // Update the TextView with the new display name
                val nameTextView: TextView = findViewById(R.id.tvUsername)
                nameTextView.text = state.username
                // Show a toast message
                Toast.makeText(this, "Username updated successfully", Toast.LENGTH_SHORT).show()
            }
            is ProfileState.ProfilePictureUpdated -> {
                // Update the ImageView with the new profile picture
                profileImageView.setImageURI(state.pictureUri)
                // Show a toast message
                Toast.makeText(this, "Profile picture updated successfully", Toast.LENGTH_SHORT).show()
            }
            is ProfileState.PasswordChanged -> {
                // Show a toast message
                Toast.makeText(this, "Password changed successfully", Toast.LENGTH_SHORT).show()
            }
            is ProfileState.AccountDeleted -> {
                // Perform navigation to the login activity, for example, as the account is now deleted
                navigateToSignInWithToast("Account deleted")

            }
            is ProfileState.Error -> {
                // Show an error message
                Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
            }
            // Add other cases for different states as needed.
//            else -> {}
        }
    }

}
