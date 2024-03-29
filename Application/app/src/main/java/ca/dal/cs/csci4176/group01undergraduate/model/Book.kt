package ca.dal.cs.csci4176.group01undergraduate.model

//importing the needed libraries
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/** a data class to hold the data that will be used in other classes.
 * @Parcelize to make it easier to accessed from other class
 * @param title the title of the book
 * @param author the author of the book
 * @param isbn a unique identifier of the book
 * @param description an overview of the book
 * @param rating the rating of the book
 * @param totalRatings the total number of ratings that the book have received.
 * @param bookBoxID the id of each book box
 * @param address the address of the book box
 */
@Parcelize
data class Book(
    val title: String,
    val author: String,
    val isbn: String,
    val description: String,
    var rating: Double,
    var totalRatings: Int,
    var bookBoxID: String,
    var address: String
    //allowing the class to be accessed between other classes.
) : Parcelable {
    constructor() : this("", "", "", "", 0.0, 0, "", "")
}
