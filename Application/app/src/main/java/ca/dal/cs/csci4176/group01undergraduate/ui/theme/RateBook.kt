package ca.dal.cs.csci4176.group01undergraduate.ui.theme

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RatingBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import ca.dal.cs.csci4176.group01undergraduate.ProfileActivity
import ca.dal.cs.csci4176.group01undergraduate.R
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class RateBook : AppCompatActivity() {
    private lateinit var databaseReference: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        FirebaseApp.initializeApp(this);
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_rate_book)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val ratingBar = findViewById<RatingBar>(R.id.bookRating) as RatingBar
        val currentRating = findViewById<RatingBar>(R.id.avgRatingValue) as RatingBar
        val bookTitle: String = findViewById<EditText>(R.id.bookTitle).toString()
        val btn = findViewById<Button>(R.id.saveRating) as Button
        databaseReference = FirebaseDatabase.getInstance().getReference("Books")

        // when the user clicks the button their rating is added and the new average rating for the book is calculated
        btn.setOnClickListener {
            // making sure a rating has been selected
            if (ratingBar != null) {
                // getting the current rating value and total number of ratings from the firebase
                val rating: String = readRating(bookTitle).toString()
                var totalRatings = readTotalRatings(bookTitle)
                // checking if the rating and total ratings for the book can be found from the firebase
                if (rating != "Not Found" && totalRatings != -1) {
                    // displaying the current user rating
                    currentRating.rating = rating.toFloat()
                    // if the rating is found then it can be converted to an integer value
                    var ratingVal: Int= rating.toInt()
                    // converting the user rating to an int value
                    val userRating : Int = ratingBar.getNumStars()
                    // calculating the new average rating value
                    totalRatings++
                    val newRating = (ratingVal + userRating).toDouble() / totalRatings.toDouble()

                    //updating the rating value and total ratings in firebase
                    databaseReference.child(bookTitle).child("Rating").setValue(newRating)
                    databaseReference.child(bookTitle).child("totalRatings").setValue(totalRatings)
                    Toast.makeText(getApplicationContext(), "Your rating: $userRating", Toast.LENGTH_SHORT).show()
                }
                // outputting an error message if the users entered book isn't found
                else {
                    Toast.makeText(getApplicationContext(), "Book not found in our database", Toast.LENGTH_LONG).show()
                }
                val intent = Intent(this, ProfileActivity::class.java)
                startActivity(intent)

            }
        }
    }
    private fun readRating(title: String): String {
        // if the book title isn't found in the database then returning not found, otherwise the current rating gets returned
        var rating: String = "Not Found"
        databaseReference = FirebaseDatabase.getInstance().getReference("Books")
        databaseReference.child(title).get().addOnSuccessListener {
            // getting the rating and total ratings data from the firebase
            if (it.exists()) {
                rating = it.child("rating").value.toString()
            }
        }
        return rating
    }

    private fun readTotalRatings(title: String): Int {
        // if the book title isn't found in the database then returning -1, otherwise the current total ratings gets returned
        var totalRatings: Int = -1
        databaseReference = FirebaseDatabase.getInstance().getReference("Books")
        databaseReference.child(title).get().addOnSuccessListener {
            // getting the rating and total ratings data from the firebase
            if (it.exists()) {
                totalRatings = it.child("totalRatings").value as Int
            }
        }
        return totalRatings
    }
}