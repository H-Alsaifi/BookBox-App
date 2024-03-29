package ca.dal.cs.csci4176.group01undergraduate.intent

/**
 * Defines a set of intents representing actions within the adding book box process.
 * These intents encapsulate user actions and requests, guiding the application's response to them.
 */
sealed class BookBoxIntent {
    /**
     * Represents a request to load the initial state or data for the adding book box process.
     * Typically used for setting up the UI to its initial state.
     */
    object Load : BookBoxIntent()

    /**
     * Represents a request to submit the details of a new book box, including its description and image.
     * @param description The text description of the book box.
     * @param pictureUri The URI of the picture associated with the book box, represented as a String.
     */
    data class SubmitDetails(val description: String, val pictureUri: String) : BookBoxIntent()

    /**
     * Represents a request to upload a picture related to the book box to a remote storage.
     * @param pictureUri The URI of the picture to be uploaded, represented as a String.
     */
    data class UploadPicture(val pictureUri: String) : BookBoxIntent()

    /**
     * Represents a request to fetch the current location of the device.
     * Used to obtain the geographic location where the book box is to be placed.
     */
    object FetchCurrentLocation : BookBoxIntent()
}
