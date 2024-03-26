package ca.dal.cs.csci4176.group01undergraduate

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProfileViewModel : ViewModel() {
    private val _state = MutableLiveData<ProfileState>(ProfileState.Idle)
    private val auth = FirebaseAuth.getInstance()
    private val usersRef = FirebaseDatabase.getInstance().getReference("users")
    private val userId =
        auth.currentUser?.uid ?: throw IllegalStateException("User must be logged in")

    val state: LiveData<ProfileState> = _state

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

//            else -> {}
        }
    }

    fun updateEmail(newEmail: String) {
        userId.let { uid ->
            usersRef.child(uid).child("email").setValue(newEmail)
                .addOnSuccessListener {
                    _state.postValue(ProfileState.EmailUpdated(newEmail))
                }
                .addOnFailureListener { error ->
                    _state.postValue(ProfileState.Error(error.message ?: "Failed to update email"))
                }
        }
    }

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

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        userId?.let { uid ->
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

    private fun calculateMembershipStatus(points: Int): String {
        return when {
            points > 60 -> "Platinum"
            points in 41..60 -> "Gold"
            points in 21..40 -> "Silver"
            else -> "Bronze"
        }
    }

}


