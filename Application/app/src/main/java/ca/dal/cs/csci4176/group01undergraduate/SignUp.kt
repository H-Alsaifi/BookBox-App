package ca.dal.cs.csci4176.group01undergraduate

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import ca.dal.cs.csci4176.group01undergraduate.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.database.FirebaseDatabase


class SignUp : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var bind: ActivitySignUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bind = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(bind.root)
        auth = FirebaseAuth.getInstance()


        bind.alreadySigned.setOnClickListener {
            val intent = Intent(this, SignIn::class.java)
            startActivity(intent)
        }

        bind.signupButton.setOnClickListener {
            val email = bind.email.text.toString().trim()
            val pass = bind.pass.text.toString().trim()
            val checkPass = bind.confirmPass.text.toString().trim()

            if (email.isNotEmpty() && pass.isNotEmpty() && checkPass.isNotEmpty()) {
                if (pass == checkPass) {
                    auth.createUserWithEmailAndPassword(email, pass)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val userId = FirebaseAuth.getInstance().currentUser?.uid
                                if (userId != null) {
                                    FirebaseDatabase.getInstance().getReference("emails").child(userId).setValue(email)
                                        .addOnCompleteListener { emailSaveTask  ->
                                            if (emailSaveTask .isSuccessful) {
                                                Toast.makeText(this, "Email saved successfully", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(this, "Failed to save email", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                }
                                Toast.makeText(this, "Signed Up Successfully!", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this, SignIn::class.java).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    putExtra("fromSignUp", true)
                                }
                                startActivity(intent)
                                finish()
                            } else {
                                val message = when (task.exception) {
                                    is FirebaseAuthWeakPasswordException -> "Password is too weak."
                                    is FirebaseAuthUserCollisionException -> "An account already exists with this email."
                                    else -> "Sign up failed."
                                }
                                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    Toast.makeText(this, "Passwords don't match.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "All fields are required.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}