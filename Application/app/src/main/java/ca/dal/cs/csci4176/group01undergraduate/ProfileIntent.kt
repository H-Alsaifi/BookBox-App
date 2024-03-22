package ca.dal.cs.csci4176.group01undergraduate

import android.net.Uri

sealed class ProfileIntent {
    data class UpdateDisplayName(val displayName: String) : ProfileIntent()
    data class UpdateProfilePicture(val pictureUri: Uri) : ProfileIntent()
    data class UpdateEmail(val email: String) : ProfileIntent()
    data class ChangePassword(val oldPassword: String, val newPassword: String) : ProfileIntent()
    object DeleteAccount : ProfileIntent()

}