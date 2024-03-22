package ca.dal.cs.csci4176.group01undergraduate.addingbookbox

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.app.ActivityCompat
import ca.dal.cs.csci4176.group01undergraduate.addingbookbox.models.BookBoxLocation
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Tasks
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import android.Manifest


class BookBoxRepository(private val context: Context) {

    private val databaseReference = FirebaseDatabase.getInstance().reference.child("bookBoxes")
    private val storageReference = FirebaseStorage.getInstance().reference.child("bookBoxPictures")
    private val fusedLocationProviderClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    suspend fun submitDetails(name: String, description: String, imageUri: Uri, location: BookBoxLocation): Result<String> {
        return try {
            val imageUrl = uploadPicture(imageUri).getOrThrow()
            val bookIDs = mutableListOf(" ", " ")

            val bookBoxDetails = mapOf(
                "name" to name,
                "description" to description,
                "imageUrl" to imageUrl.toString(),
                "latitude" to location.latitude,
                "longitude" to location.longitude,
                "bookIDs" to bookIDs
            )


            val pushReference = databaseReference.push()
            pushReference.setValue(bookBoxDetails).await()
            Result.success(pushReference.key ?: "Unknown Key")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadPicture(pictureUri: Uri): Result<Uri> {
        val pictureRef = storageReference.child(pictureUri.lastPathSegment ?: "unknown_${System.currentTimeMillis()}")
        return try {
            pictureRef.putFile(pictureUri).await()
            val downloadUri = pictureRef.downloadUrl.await()
            Result.success(downloadUri)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCurrentLocation(): Result<BookBoxLocation> = suspendCancellableCoroutine { continuation ->
        // Check for permissions before accessing the location.
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is not granted, resume with an exception.
            continuation.resume(Result.failure(SecurityException("Location permissions not granted")))
            return@suspendCancellableCoroutine
        }

        // Now we can safely call lastLocation because we know the permission has been granted.
        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                continuation.resume(Result.success(BookBoxLocation(location.latitude, location.longitude)))
            } else {
                continuation.resume(Result.failure(Exception("Location is not available")))
            }
        }.addOnFailureListener { exception ->
            continuation.resumeWithException(exception)
        }
    }


    fun hasLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

}