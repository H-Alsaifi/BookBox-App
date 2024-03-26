package ca.dal.cs.csci4176.group01undergraduate


import android.graphics.PixelFormat
import android.os.Handler
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import ca.dal.cs.csci4176.group01undergraduate.databinding.ActivityResetPasswordBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ResetPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResetPasswordBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResetPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.submitResetButton.setOnClickListener {
            val email = binding.emailEditText.text.toString().trim()
            if (email.isNotEmpty()) {
                FirebaseDatabase.getInstance().getReference("emails")
                    .orderByValue().equalTo(email).addListenerForSingleValueEvent(object :
                        ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                // Email exists, proceed to send reset link
                                auth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        displayMessage("Reset link sent to your email.")
//                                        Toast.makeText(applicationContext, "Reset link sent to your email.", Toast.LENGTH_LONG).show()
                                        finish()
                                    } else {
                                        displayMessage("Failed to send reset link.")
//                                        Toast.makeText(applicationContext, "Failed to send reset link.", Toast.LENGTH_LONG).show()
                                    }
                                }
                            } else {
                                // Email not registered
                                displayMessage("Email not recognized.")
//                                Toast.makeText(applicationContext, "Email not recognized.", Toast.LENGTH_LONG).show()
                            }
                        }


                        override fun onCancelled(databaseError: DatabaseError) {
                            // Handle possible errors
                            Toast.makeText(applicationContext, "Error: ${databaseError.message}", Toast.LENGTH_LONG).show()
                        }
                    })
            } else {
                displayMessage("Please enter your email.")
//                Toast.makeText(applicationContext, "Please enter your email.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun displayMessage(message: String) {
        val messageView = layoutInflater.inflate(R.layout.display_message, null) as TextView
        messageView.text = message

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.CENTER

        val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        windowManager.addView(messageView, params)

        Handler().postDelayed({
            windowManager.removeView(messageView)
        }, 2000)
    }

}
