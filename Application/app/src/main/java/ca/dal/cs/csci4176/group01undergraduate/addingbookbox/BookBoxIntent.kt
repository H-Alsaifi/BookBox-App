package ca.dal.cs.csci4176.group01undergraduate.addingbookbox

sealed class BookBoxIntent {
    object Load : BookBoxIntent()
    data class SubmitDetails(val name: String, val description: String, val pictureUri: String) : BookBoxIntent()
    data class UploadPicture(val pictureUri: String) : BookBoxIntent()
    object FetchCurrentLocation : BookBoxIntent()
}
