package ca.dal.cs.csci4176.group01undergraduate.addingbookbox.models

import android.net.Uri
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import com.google.firebase.storage.FirebaseStorage

class AddBookBoxModel {

    // References to the Firebase Realtime Database and Firebase Storage
    private val databaseReference = FirebaseDatabase.getInstance().reference
    private val storageReference = FirebaseStorage.getInstance().reference

    /**
     * Submits the details of a new book box to the Firebase Database and uploads the associated image to Firebase Storage.
     * @param location The geographic location of the book box.
     * @param description A text description of the book box. Defaults to "No Description" if not provided.
     * @param imageUri The URI of the image to be uploaded for the book box.
     * @return A [Result] containing the unique key of the new book box entry in the database if successful, or an exception if not.
     */
    suspend fun submitDetails(location: BookBoxLocation, description: String = "No Description", imageUri: Uri): Result<String> {

        return try {
            // First, upload the image to Firebase Storage and get the URL.
            val imageUrlResult = uploadPicture(imageUri)
            val imageUrl = imageUrlResult.getOrThrow()

            // Prepare the details to be stored in the database.
            val bookBoxDetails = hashMapOf(
                "latitude" to location.latitude,
                "longitude" to location.longitude,
                "description" to if (description.isBlank()) "No Description" else description,
                "imageUrl" to imageUrl.toString()
            )

            // Create a new entry in the 'bookBoxes' node and set its value to the book box details.
            val pushReference = databaseReference.child("bookBoxes").push()
            pushReference.setValue(bookBoxDetails).await()
            // Return the unique key of the new book box entry.
            Result.success(pushReference.key ?: "Unknown Key")
        } catch (e: Exception) {
            // In case of failure, return a Result object encapsulating the exception.
            Result.failure(e)
        }
    }

    /**
     * Uploads an image to Firebase Storage and returns the download URL.
     * @param pictureUri The URI of the picture to upload.
     * @return A [Result] containing the URI of the uploaded image if successful, or an exception if not.
     */
    suspend fun uploadPicture(pictureUri: Uri): Result<Uri> {
        // Generate a file name based on the last path segment of the URI, or a timestamp if unavailable.
        val fileName = pictureUri.lastPathSegment ?: "unknown_${System.currentTimeMillis()}"
        val pictureRef = storageReference.child("bookBoxPictures/$fileName")
        return try {
            // Upload the file to Firebase Storage and await the result.
            pictureRef.putFile(pictureUri).await()
            // Once uploaded, get and return the download URL of the image.
            val downloadUri = pictureRef.downloadUrl.await()
            Result.success(downloadUri)
        } catch (e: Exception) {
            // In case of failure, encapsulate the exception in a Result object and return it.
            Result.failure(e)
        }
    }
}
