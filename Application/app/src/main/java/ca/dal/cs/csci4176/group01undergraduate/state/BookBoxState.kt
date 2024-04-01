package ca.dal.cs.csci4176.group01undergraduate.state

import ca.dal.cs.csci4176.group01undergraduate.model.BookBoxLocation

/**
 * Represents the state of the book box adding process in the application.
 *
 * @property isLoading Indicates if there is an ongoing process, such as uploading an image or submitting details to the database.
 * @property location The location of the book box being added. It is nullable as it might not be available at all steps of the process.
 * @property imageUrl The URL of the image that has been uploaded for the book box. It is nullable as it might not be available immediately.
 * @property documentId The unique identifier of the book box entry in the database. It is nullable as it is only available after successful submission.
 * @property error Holds any exception that might have occurred during the process. It is nullable as errors might not always occur.
 * @property isSuccessful Indicates whether the book box adding process was successful.
 */
data class BookBoxState(
    val isLoading: Boolean = false,
    val location: BookBoxLocation? = null,
    val imageUrl: String? = null,
    val documentId: String? = null,
    val error: Exception? = null,
    val isSuccessful: Boolean = false
)
