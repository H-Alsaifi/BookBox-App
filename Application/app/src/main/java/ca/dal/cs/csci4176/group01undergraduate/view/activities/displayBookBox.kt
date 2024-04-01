package ca.dal.cs.csci4176.group01undergraduate.view.activities

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import ca.dal.cs.csci4176.group01undergraduate.R
import ca.dal.cs.csci4176.group01undergraduate.model.BookBoxLocation
import ca.dal.cs.csci4176.group01undergraduate.view.fragments.BoxFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.net.HttpURLConnection
import java.net.URL

class displayBookBox : AppCompatActivity() {
    private lateinit var databaseReference: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_display_book_box)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val bookBoxImage: ImageView = findViewById(R.id.bookBoxImage)
        val boxName: EditText = findViewById<EditText?>(R.id.boxName)
        val boxDesc: EditText = findViewById<EditText?>(R.id.boxDesc)
        val favBtn: Button = findViewById(R.id.favouriteBox)
        val backBtn: Button = findViewById(R.id.backBtn)

        // displays the details of the book box that was passed from the intent
        val name = intent.getStringExtra("name")
        val description = intent.getStringExtra("description")
        Thread {
            val bitmap = downloadImage(intent.getStringExtra("imageUrl"))
            runOnUiThread {
                bookBoxImage.setImageBitmap(bitmap)
            }
        }.start()



        // getting the firebase reference to the users
        var db: FirebaseDatabase= FirebaseDatabase.getInstance()
        databaseReference = db.getReference()
        databaseReference.child("users")

        // displaying the book box information for the user
        boxName.setText(name)
        boxDesc.setText(description)

        // if the user clicks on the bookbox then storing it under their favourites in firebase
        favBtn.setOnClickListener() {
//            // getting the id of the currently logged in user to save the favorite in their account
//            val userId = FirebaseAuth.getInstance().currentUser?.uid.toString()
//            val loc = bookBox.location
//            val userRef = FirebaseDatabase.getInstance().getReference("users").child(userId)
//            // saving the coords of the favorite box in the users favorites
//            val BookBoxLocation: BookBoxLocation? = loc?.let { it1 -> location?.let { it2 ->
//                BookBoxLocation(it1.longitude,
//                    it2.latitude)
//            } }
//            // saving the new favorite book box coordinates
//            userRef.child("favorites").push().setValue(BookBoxLocation)
        }

        // allowing the user to go back to their previous activity (box fragment)
        backBtn.setOnClickListener {
            val fragment = BoxFragment()
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit()
        }
    }

    // Downloads an image from a URL and returns it as a Bitmap.
    private fun downloadImage(urlString: String?): Bitmap? {
        if (urlString == null) return null // Return null if the URL is not provided.
        var bitmap: Bitmap? = null
        try {
            // Open a connection to the URL and decode the stream into a Bitmap.
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val inputStream = connection.inputStream
            bitmap = BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            e.printStackTrace() // Log any exceptions during the image download process.
        }
        return bitmap
    }
}