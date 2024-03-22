package ca.dal.cs.csci4176.group01undergraduate.displayingbookbox

import ca.dal.cs.csci4176.group01undergraduate.addingbookbox.models.BookBoxLocation

data class BookBox(
    val name: String? = null,
    // Replace the String type with the BookBoxLocation class for the location
    val location: BookBoxLocation? = null,
    val description: String? = null,
    val imageUrl: String? = null
)
