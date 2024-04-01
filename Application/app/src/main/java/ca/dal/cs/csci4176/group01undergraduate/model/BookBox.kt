package ca.dal.cs.csci4176.group01undergraduate.model


/**
 * Data class representing a book box.
 *
 * @property location The geographic location of the book box.
 * @property address The string address corresponding with the location
 * @property description A text description of the book box. It could include details such as content or instructions for use.
 * @property imageUrl A URL pointing to an image of the book box. This could be used for displaying in the UI.
 * @property bookIDs A mutable list of identifiers for books contained in the book box. These IDs can be used to fetch additional details about each book.
 */
data class BookBox(
    val location: BookBoxLocation? = null,
    val address: String? = null,
    val description: String? = null,
    val imageUrl: String? = null,
    val bookIDs: MutableList<String> = mutableListOf()
)
