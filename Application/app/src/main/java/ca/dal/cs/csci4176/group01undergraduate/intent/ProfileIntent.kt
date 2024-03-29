package ca.dal.cs.csci4176.group01undergraduate.intent

import android.net.Uri

sealed class ProfileIntent {
    //data class to represent the intent of updating the display name
    data class UpdateDisplayName(val displayName: String) : ProfileIntent()
    //data class to represent the intent of updating the profile picture
    data class UpdateProfilePicture(val pictureUri: Uri) : ProfileIntent()
    //data class to represent the intent of updating the user's email address.
    data class UpdateEmail(val email: String) : ProfileIntent()
    //data class to represent the intent of changing the user's password.
    data class ChangePassword(val oldPassword: String, val newPassword: String) : ProfileIntent()
    //object to represent the intent of deleting the user's account.
    object DeleteAccount : ProfileIntent()

}