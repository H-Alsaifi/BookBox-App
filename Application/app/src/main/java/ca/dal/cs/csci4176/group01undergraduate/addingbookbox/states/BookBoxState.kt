package ca.dal.cs.csci4176.group01undergraduate.addingbookbox.states

import ca.dal.cs.csci4176.group01undergraduate.addingbookbox.models.BookBoxLocation

data class BookBoxState(
    val isLoading: Boolean = false,
    val location: BookBoxLocation? = null,
    val imageUrl: String? = null,
    val documentId: String? = null,
    val error: Exception? = null,
    val isSuccessful: Boolean = false
)


