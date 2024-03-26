package ca.dal.cs.csci4176.group01undergraduate

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import ca.dal.cs.csci4176.group01undergraduate.displayingbookbox.BoxFragment
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

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
        val boxName: EditText = findViewById<EditText?>(R.id.boxName)
        val boxDesc: EditText = findViewById<EditText?>(R.id.boxDesc)
        val favBtn: Button = findViewById(R.id.favouriteBox)
        val backBtn: Button = findViewById(R.id.backBtn)

        val name = intent.getStringExtra("name")
        val description = intent.getStringExtra("description")
        val imageUrl = intent.getStringExtra("imageUrl")

        databaseReference = FirebaseDatabase.getInstance().getReference("users")

        boxName.setText(name)
        boxDesc.setText(description)

        // if the user clicks on the bookbox then storing it under their favourites in firebase
        favBtn.setOnClickListener() {
            // has to be switched to key value
            databaseReference.child("favourites").setValue(boxName)
        }

        // allowing the user to go back to their previous activity (box fragment)
        backBtn.setOnClickListener {
            val fragment = BoxFragment()
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit()
        }
    }
}