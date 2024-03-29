package ca.dal.cs.csci4176.group01undergraduate.response

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
    val averageRating: Double? = null,
    val ratingsCount: Int? = 0
)
data class IndustryIdentifier(
    val type: String,
    val identifier: String
)