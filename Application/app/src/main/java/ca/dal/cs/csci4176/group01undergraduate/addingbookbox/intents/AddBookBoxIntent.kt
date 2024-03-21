package ca.dal.cs.csci4176.group01undergraduate.addingbookbox.intents

import android.net.Uri

sealed class AddBookBoxIntent {
    object OpenAddBookBox : AddBookBoxIntent()
    data class SubmitDetails(val name: String, val location: String, val description: String, val pictureUri: Uri) : AddBookBoxIntent()
    data class UploadPicture(val pictureUri: Uri) : AddBookBoxIntent()
    object ConfirmAddition : AddBookBoxIntent()
}
