package ca.dal.cs.csci4176.group01undergraduate.addingbookbox.models

import android.net.Uri
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import com.google.firebase.storage.FirebaseStorage

class AddBookBoxModel {

    private val databaseReference = FirebaseDatabase.getInstance().reference
    private val storageReference = FirebaseStorage.getInstance().reference

    suspend fun submitDetails(name: String, location: BookBoxLocation, description: String, imageUri: Uri): Result<String> {
        return try {
            val imageUrlResult = uploadPicture(imageUri)
            val imageUrl = imageUrlResult.getOrThrow()

            val bookBoxDetails = hashMapOf(
                "name" to name,
                "latitude" to location.latitude,
                "longitude" to location.longitude,
                "description" to description,
                "imageUrl" to imageUrl.toString()
            )

            val pushReference = databaseReference.child("bookBoxes").push()
            pushReference.setValue(bookBoxDetails).await()
            Result.success(pushReference.key ?: "Unknown Key")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadPicture(pictureUri: Uri): Result<Uri> {
        // Check if lastPathSegment is not null
        val fileName = pictureUri.lastPathSegment ?: "unknown_${System.currentTimeMillis()}"
        val pictureRef = storageReference.child("bookBoxPictures/$fileName")
        return try {
            val uploadTask = pictureRef.putFile(pictureUri).await()
            val downloadUri = pictureRef.downloadUrl.await()
            Result.success(downloadUri)
        } catch (e: Exception) {
            // Log the error or handle it as per your app's error handling policy
            Result.failure(e)
        }
    }
}
