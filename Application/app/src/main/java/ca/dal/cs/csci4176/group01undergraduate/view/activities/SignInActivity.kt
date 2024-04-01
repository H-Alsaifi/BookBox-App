package ca.dal.cs.csci4176.group01undergraduate.view.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import ca.dal.cs.csci4176.group01undergraduate.R
import ca.dal.cs.csci4176.group01undergraduate.databinding.ActivitySignInBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException

class SignInActivity : AppCompatActivity() {

    //initializing variables for firebase and binding
    private lateinit var auth: FirebaseAuth
    private lateinit var bind: ActivitySignInBinding

    //onCreate starts when activity starts
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //inflates the layout by the generated binding class
        bind = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(bind.root)

        auth = FirebaseAuth.getInstance()

        //setting an onClickListener that navigates to signup page when pressed on
        bind.textView.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

        //setting an onClickListener for the signin button
        bind.button.setOnClickListener {
            val email = bind.emailEt.text.toString()
            val pass = bind.passET.text.toString()

            //checking if the email and password are empty
            if (email.isNotEmpty() && pass.isNotEmpty()) {
                //using firebase function to sign in with email and password
                auth.signInWithEmailAndPassword(email, pass).addOnCompleteListener { task->
                    //check if sign in is successful
                    if (task.isSuccessful) {
                        //setting a stay sign in button
                        val staySign = bind.staySignedInButton.isChecked
                        getSharedPreferences("prefs", Context.MODE_PRIVATE)
                            .edit().putBoolean("staySign", staySign).apply()
                        Toast.makeText(this, "SignIn Successful", Toast.LENGTH_SHORT).show()

                        // saving the user email in shared preferences for accessing their saved favourites later
                        val sharedPref = getSharedPreferences("MyData", MODE_PRIVATE)
                        val editor = sharedPref.edit()
                        editor.putString("email", email)
                        editor.apply()
                        //navigating to main activity if it is successful
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                        //otherwise send a toast
                    } else {
                        val message = when (task.exception) {
                            is FirebaseAuthInvalidCredentialsException -> "Invalid email or password."
                            is FirebaseAuthInvalidUserException -> "No account found with this email."
                            else -> "Invalid Credentials!"
                        }
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Please fill the fields!", Toast.LENGTH_SHORT).show()
            }
        }

        //setting a forgot password button for the user that navigates to forgot password activity
        val forgotPasswordTextView = findViewById<TextView>(R.id.forgot_password)
        forgotPasswordTextView.setOnClickListener {
            startActivity(Intent(this, ResetPasswordActivity::class.java))
        }
    }

    /**
     * a method to be called when the stay signed in button is pressed
     */
    override fun onStart() {
        super.onStart()

        val staySign = getSharedPreferences("prefs", Context.MODE_PRIVATE)
            .getBoolean("staySign", false)

        //check if the user is legit and available, and chooses to stay logged in or not
        if(auth.currentUser != null && !checkSignUp() && staySign){
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    /**
     * a method to check if the user has navigated from the sign up activity
     * @return signup check user is returning correctly
     */
    private fun checkSignUp(): Boolean{
        val signUp = intent.getBooleanExtra("fromSignUp", false)
        intent.removeExtra("fromSignUp")
        return signUp
    }
}