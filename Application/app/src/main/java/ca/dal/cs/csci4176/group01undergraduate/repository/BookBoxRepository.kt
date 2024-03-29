package ca.dal.cs.csci4176.group01undergraduate.repository

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.app.ActivityCompat
import ca.dal.cs.csci4176.group01undergraduate.model.BookBoxLocation
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import android.Manifest
import android.location.Geocoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.Locale


/**
 * Repository class responsible for handling data operations for book boxes.
 * It interacts with Firebase Realtime Database and Firebase Storage for storing book box details and images,
 * and uses the Fused Location Provider for location data.
 *
 * @property context Context used for accessing the Fused Location Provider.
 */
class BookBoxRepository(private val context: Context) {

    // Reference to the 'bookBoxes' node in Firebase Realtime Database
    private val databaseReference = FirebaseDatabase.getInstance().reference.child("bookBoxes")
    // Reference to the 'bookBoxPictures' folder in Firebase Storage
    private val storageReference = FirebaseStorage.getInstance().reference.child("bookBoxPictures")
    // Client for accessing the Fused Location Provider
    private val fusedLocationProviderClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    /**
     * Submits the details of a book box to Firebase Database and uploads its image to Firebase Storage.
     *
     * @param imageUri URI of the image to upload.
     * @param location Location of the book box including latitude and longitude.
     * @param description Description of the book box. Defaults to "No Description" if not provided.
     * @return A [Result] containing the unique key of the newly added book box entry in the database if successful, or an exception if failed.
     */
    suspend fun submitDetails(imageUri: Uri, location: BookBoxLocation, description: String = "No Description"): Result<String> {
        // Attempt to convert coordinates to address, use "N/A" as fallback
        val address = convertCoordinatesToAddress(location.latitude, location.longitude).getOrDefault("N/A")

        // Proceed with submitting the book box details
        return try {
            val imageUrl = uploadPicture(imageUri).getOrThrow()
            val bookIDs = mutableListOf<String>()
            val bookBoxDetails = mapOf(
                "description" to if (description.isBlank()) "No Description" else description,
                "imageUrl" to imageUrl.toString(),
                "latitude" to location.latitude,
                "longitude" to location.longitude,
                "bookIDs" to bookIDs,
                "address" to address
            )

            // Push the new book box entry to the database and store its details
            val pushReference = databaseReference.push()
            pushReference.setValue(bookBoxDetails).await()

            // Return the unique key of the new entry
            Result.success(pushReference.key ?: "Unknown Key")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }



    /**
     * Uploads an image to Firebase Storage and returns the URL to access it.
     *
     * @param pictureUri URI of the picture to be uploaded.
     * @return A [Result] containing the URL of the uploaded image if successful, or an exception if failed.
     */
    suspend fun uploadPicture(pictureUri: Uri): Result<Uri> {
        // Generate a file name based on the URI's last segment or a timestamp if unavailable
        val pictureRef = storageReference.child(pictureUri.lastPathSegment ?: "unknown_${System.currentTimeMillis()}")
        return try {
            // Upload the file to Firebase Storage
            pictureRef.putFile(pictureUri).await()
            // Retrieve and return the download URL of the uploaded file
            val downloadUri = pictureRef.downloadUrl.await()
            Result.success(downloadUri)
        } catch (e: Exception) {
            // Return an error result in case of failure
            Result.failure(e)
        }
    }

    /**
     * Fetches the current location of the device using the Fused Location Provider.
     * Requires location permissions to be granted.
     *
     * @return A [Result] containing the current [BookBoxLocation] if successful, or an exception if failed or permissions are not granted.
     */
    suspend fun getCurrentLocation(): Result<BookBoxLocation> = suspendCancellableCoroutine { continuation ->
        // Check for location permissions before accessing the device's location
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Resume with a failure result if permissions are not granted
            continuation.resume(Result.failure(SecurityException("Location permissions not granted")))
            return@suspendCancellableCoroutine
        }

        // Request the last known location and resume the coroutine with the result
        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                // Resume with a success result containing the current location
                continuation.resume(Result.success(BookBoxLocation(location.latitude, location.longitude)))
            } else {
                // Resume with a failure result if the location is unavailable
                continuation.resume(Result.failure(Exception("Location is not available")))
            }
        }.addOnFailureListener { exception ->
            // Resume with an exception in case of failure
            continuation.resumeWithException(exception)
        }
    }

    /**
     * Converts the latitude and longitude to an address string
     * @return Address String
     */
    suspend fun convertCoordinatesToAddress(latitude: Double, longitude: Double): Result<String> = suspendCancellableCoroutine { continuation ->
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)

            if (addresses != null) {
                if (addresses.isNotEmpty()) {
                    val address = addresses[0]
                    val addressString = address.getAddressLine(0)
                    continuation.resume(Result.success(addressString))
                } else {
                    continuation.resume(Result.failure(Exception("No address found")))
                }
            }
        } catch (e: Exception) {
            continuation.resumeWithException(e)
        }
    }
}

