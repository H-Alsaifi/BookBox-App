package ca.dal.cs.csci4176.group01undergraduate.model

import android.net.Uri

data class User(
    val username: String = "",
    val email: String = "",
    val points: Int = 0,
    val rank: String = "Iron",
    val profilePictureUri: String = "",
    val bookIds: List<String> = emptyList(),
    val bookBoxIds: List<String> = emptyList(),
    val favoriteBookBoxes: List<String> = emptyList()
){
    constructor(): this("", "", 0, "Iron",  "",emptyList(), emptyList(), emptyList())
}
open class ProfileState {
    object Idle : ProfileState()
    data class DisplayNameUpdated(val username: String) : ProfileState()
    data class ProfilePictureUpdated(val pictureUri: Uri) : ProfileState()
    data class EmailUpdated(val email: String) : ProfileState()
    object PasswordChanged : ProfileState()
    object AccountDeleted : ProfileState()
    data class Error(val message: String) : ProfileState()
    data class MembershipStatusUpdated(val status: String) : ProfileState()
}