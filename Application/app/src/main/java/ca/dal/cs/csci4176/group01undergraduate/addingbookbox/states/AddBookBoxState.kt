package ca.dal.cs.csci4176.group01undergraduate.addingbookbox.states

data class AddBookBoxState(
    val isLoading: Boolean = false,
    val isSuccessful: Boolean = false,
    val error: Exception? = null,
    val imageUrl: String? = null,  // this line to keep track of the image URL
    val documentId: String? = null
)
