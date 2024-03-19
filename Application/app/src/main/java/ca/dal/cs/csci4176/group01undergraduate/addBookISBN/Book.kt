package ca.dal.cs.csci4176.group01undergraduate.addBookISBN

data class Book(
    val title: String,
    val author: String,
    val isbn: String,
    val description: String,
    val rating: Int
) {
    // Default no-argument constructor required by Firebase
    constructor() : this("", "", "", "", 0)
}