package ca.dal.cs.csci4176.group01undergraduate

data class User(
    val username: String,
    val email: String,
    val points: Int = 0,
    val rank: String = "Iron",
    val bookIds: List<String> = emptyList(),
    val bookBoxIds: List<String> = emptyList(),
    val favoriteBookBoxes: List<String> = emptyList()
)
