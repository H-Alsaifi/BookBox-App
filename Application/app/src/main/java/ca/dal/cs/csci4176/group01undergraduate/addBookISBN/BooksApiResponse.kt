package ca.dal.cs.csci4176.group01undergraduate.addBookISBN

data class BooksApiResponse(
    val items: List<BookItem>?
)

data class BookItem(
    val volumeInfo: VolumeInfo
)

data class VolumeInfo(
    val title: String?,
    val authors: List<String>?,
    val description: String?,
    val industryIdentifiers: List<IndustryIdentifier>?,
    // Add other fields as needed
)

data class IndustryIdentifier(
    val type: String,
    val identifier: String
)