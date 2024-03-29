package ca.dal.cs.csci4176.group01undergraduate.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ca.dal.cs.csci4176.group01undergraduate.intent.ProfileIntent
import ca.dal.cs.csci4176.group01undergraduate.model.ProfileState
import ca.dal.cs.csci4176.group01undergraduate.model.User
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

//ProfileViewModel extends ViewModel, managing UI-related data
class ProfileViewModel : ViewModel() {
    //mutableLiveData to handle changes in profile states
    private val _state = MutableLiveData<ProfileState>(ProfileState.Idle)
    //firebase Authentication instance for user authentication tasks
    private val auth = FirebaseAuth.getInstance()
    //reference to the 'users' node in Firebase Database for accessing user data
    private val usersRef = FirebaseDatabase.getInstance().getReference("users")
    //get the current user's ID from FirebaseAuth.
    private val userId =
        auth.currentUser?.uid ?: throw IllegalStateException("User must be logged in")
    val state: LiveData<ProfileState> = _state
    //handles intents representing actions to be performed on the user profile
    fun handleIntent(intent: ProfileIntent) {
        when (intent) {
            is ProfileIntent.UpdateDisplayName -> updateDisplayName(intent.displayName)
            is ProfileIntent.UpdateEmail -> updateEmail(intent.email)
            is ProfileIntent.UpdateProfilePicture -> updateProfilePicture(intent.pictureUri)
            is ProfileIntent.ChangePassword -> changePassword(
                intent.oldPassword,
                intent.newPassword
            )
            is ProfileIntent.DeleteAccount -> deleteAccount()
        }
    }
    //updates the user's email in Firebase and posts the result to the profile state
    private fun updateEmail(newEmail: String) {
        userId.let { uid ->
            usersRef.child(uid).child("email").setValue(newEmail)
                .addOnSuccessListener {
                    //notify observers of the email update success.
                    _state.postValue(ProfileState.EmailUpdated(newEmail))
                }
                .addOnFailureListener { error ->
                    _state.postValue(ProfileState.Error(error.message ?: "Failed to update email"))
                }
        }
    }
    //updates the user's display name in Firebase and posts the result to the profile state
    fun updateDisplayName(displayName: String) {
        userId.let { uid ->
            usersRef.child(uid).child("displayName").setValue(displayName)
                .addOnSuccessListener {
                    _state.postValue(ProfileState.DisplayNameUpdated(displayName))
                }
                .addOnFailureListener { error ->
                    _state.postValue(
                        ProfileState.Error(
                            error.message ?: "Failed to update display name"
                        )
                    )
                }
        }
    }
    //Updates the user's profile picture in Firebase and posts the result to the profile state
    fun updateProfilePicture(pictureUri: Uri) {
        userId.let { uid ->
            usersRef.child(uid).child("profilePictureUri").setValue(pictureUri.toString())
                .addOnSuccessListener {
                    _state.postValue(ProfileState.ProfilePictureUpdated(pictureUri))
                }
                .addOnFailureListener { error ->
                    _state.postValue(
                        ProfileState.Error(
                            error.message ?: "Failed to update profile picture"
                        )
                    )
                }
        }
    }
    //changes the user's password after re-authentication.
    fun changePassword(oldPassword: String, newPassword: String) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null && user.email != null) {
            val credential = EmailAuthProvider.getCredential(user.email!!, oldPassword)
            user.reauthenticate(credential).addOnCompleteListener { reauthTask ->
                if (reauthTask.isSuccessful) {
                    user.updatePassword(newPassword).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            _state.postValue(ProfileState.PasswordChanged)
                        } else {
                            _state.postValue(ProfileState.Error("Password update failed: ${task.exception?.message}"))
                        }
                    }
                } else {
                    _state.postValue(ProfileState.Error("Re-authentication failed: ${reauthTask.exception?.message}"))
                }
            }
        }
    }
    //deletes the user's account from Firebase and posts the result to the profile state
    fun deleteAccount() {
        userId.let { uid ->
            auth.currentUser?.delete()
                ?.addOnSuccessListener {
                    usersRef.child(uid).removeValue()
                        .addOnSuccessListener {
                            _state.postValue(ProfileState.AccountDeleted)
                        }
                }
                ?.addOnFailureListener { error ->
                    _state.postValue(
                        ProfileState.Error(
                            error.message ?: "Failed to delete account"
                        )
                    )
                }
        }
    }
    //initializer block to load the user's profile at ViewModel initialization
    init {
        loadUserProfile()
    }
    //loads the user's profile data from Firebase and updates the profile state accordingly
    fun loadUserProfile() {
        userId.let { uid ->
            FirebaseDatabase.getInstance().getReference("users").child(uid)
                .get().addOnSuccessListener { snapshot ->
                    val user = snapshot.getValue(User::class.java)
                    user?.let {
                        _state.value = ProfileState.DisplayNameUpdated(it.username)
                        _state.value = ProfileState.EmailUpdated(it.email)
                        _state.value =
                            ProfileState.MembershipStatusUpdated(calculateMembershipStatus(it.points))
                    }
                }.addOnFailureListener {
                    _state.value = ProfileState.Error("Failed to fetch user data")
                }
        }
    }
    //calculates the membership status based on points and updates the profile state.
    private fun calculateMembershipStatus(points: Int): String {
        return when {
            points > 60 -> "Platinum"
            points in 41..60 -> "Gold"
            points in 21..40 -> "Silver"
            else -> "Bronze"
        }
    }
}