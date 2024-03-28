package ca.dal.cs.csci4176.group01undergraduate

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.RatingBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class RateBook : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        FirebaseApp.initializeApp(this)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_rate_book)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // initializing all the xml features necessary
        val ratingBar = findViewById<RatingBar>(R.id.bookRating) as RatingBar
        val backBtn = findViewById<Button>(R.id.backBtn) as Button
        val bookTitle: String = findViewById<EditText>(R.id.bookTitle).toString()
        val rateBtn = findViewById<Button>(R.id.saveRating) as Button

        // when the user clicks the button getting their rating and updating the new average rating with the set rating function
        rateBtn.setOnClickListener {
            val userRating: Int = ratingBar.rating.toInt()
            setRating(userRating, bookTitle, this)
        }
        // button to return the user to the previous page
        backBtn.setOnClickListener {
            // the previous page is the profile fragment
            val fragment = ProfileFragment()
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit()
        }
    }

    // function takes the users rating and the name of the book being rated and updates its average rating value
    private fun setRating(userRating: Int, title: String, Context: Context) {
        // getting a new reference to to find the book titles
        val databaseReference: DatabaseReference =
            FirebaseDatabase.getInstance().getReference("Book")

        // looking through the elements of the database
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // checking if the book title matches the given title
                val name = snapshot.child("title").value.toString()
                if (name.equals(title, true)) {
                    // getting the avg and total ratings from the database
                    var avgrating = snapshot.child("rating").value.toString().toDouble()
                    Toast.makeText(Context, "Current Book Rating: $avgrating", Toast.LENGTH_SHORT)
                        .show()
                    var totalRatings = snapshot.child("totalRatings").value.toString().toInt()
                    // calculating the new average rating user weighted ratings with the total ratings
                    avgrating = ((avgrating * totalRatings) + userRating) / (totalRatings + 1)
                    databaseReference.child("rating").setValue(avgrating)
                    // increasing the total ratings value and updating it in the database
                    totalRatings++
                    databaseReference.child("totalRatings").setValue(totalRatings)
                    // displaying feedback for the user and the updated rating
                    Toast.makeText(Context, "New Book Rating: $avgrating", Toast.LENGTH_LONG).show()
                }
            }

            // giving some user feedback if the book title isn't found in the database
            override fun onCancelled(error: DatabaseError) {
                Log.w(ContentValues.TAG, "Failed to read value.", error.toException())
                Toast.makeText(Context, "Couldn't Find Book", Toast.LENGTH_LONG).show()
            }

        })
    }
}
