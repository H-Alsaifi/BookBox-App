package ca.dal.cs.csci4176.group01undergraduate

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import ca.dal.cs.csci4176.group01undergraduate.displayingbookbox.BoxFragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class displayBookBox : AppCompatActivity() {
    val DEFAULT = "N/A"
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
        val boxName: EditText = findViewById<EditText?>(R.id.boxName)
        val boxDesc: EditText = findViewById<EditText?>(R.id.boxDesc)
        val favBtn: Button = findViewById(R.id.favouriteBox)
        val backBtn: Button = findViewById(R.id.backBtn)

        // displays the details of the book box that was passed from the intent
        val name = intent.getStringExtra("name")
        val description = intent.getStringExtra("description")
        //val imageUrl = intent.getStringExtra("imageUrl")

        // getting the users email from shared preferences so the saved book box can be stored under their favorites
        val SharedPreferences: SharedPreferences = getSharedPreferences("MyData", MODE_PRIVATE)
        val email: String? = SharedPreferences.getString("email",DEFAULT)

        // getting the firebase reference to the users
        val db: FirebaseDatabase= FirebaseDatabase.getInstance()
        databaseReference = db.getReference().child("users")

        // displaying the book box information for the user
        boxName.setText(name)
        boxDesc.setText(description)

//        /// if the user clicks on the bookbox then storing it under their favourites in firebase
//        favBtn.setOnClickListener() {
//            databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
//                override fun onDataChange(snapshot: DataSnapshot) {
//                    // reading each users email until the current users account is found
//                    val userEmail = snapshot.child("email").value.toString()
//                    if (userEmail.equals(email, true)) {
//                        // adding the selected book box to the users favourites
//                        // has to be switched to key value or bookbox id
//                        databaseReference.child("favourites").setValue(boxName)
//                    }
//                }
//                override fun onCancelled(error: DatabaseError) {
//
//                }
//
//            })
//        }

        // allowing the user to go back to their previous activity (box fragment)
        backBtn.setOnClickListener {
            val fragment = BoxFragment()
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit()
        }
    }
}