package ca.dal.cs.csci4176.group01undergraduate

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class ResetPassword : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)

        auth = FirebaseAuth.getInstance()

        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val submitResetButton = findViewById<Button>(R.id.submitResetButton)

        submitResetButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            if (email.isNotEmpty()) {
                auth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Reset link sent to your email.", Toast.LENGTH_LONG).show()
                        finish()
                    } else {
                        Toast.makeText(this, "Failed to send reset link.", Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                Toast.makeText(this, "Please enter your email.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
