package ca.dal.cs.csci4176.group01undergraduate.view.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import ca.dal.cs.csci4176.group01undergraduate.model.User
import ca.dal.cs.csci4176.group01undergraduate.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.database.FirebaseDatabase


class SignUpActivity : AppCompatActivity() {

    //initializing variables for firebase and binding
    private lateinit var auth: FirebaseAuth
    private lateinit var bind: ActivitySignUpBinding

    /**
     * a method that is run when the activity starts and created
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bind = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(bind.root)
        auth = FirebaseAuth.getInstance()

        //setting an onClickListener that navigates to signin page when pressed on
        bind.alreadySigned.setOnClickListener {
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
        }

        //setting an onClickListener for the sign in button
        bind.signupButton.setOnClickListener {
            //variables to get input from the user and removing the spaces
            val email = bind.email.text.toString().trim()
            val pass = bind.pass.text.toString().trim()
            val checkPass = bind.confirmPass.text.toString().trim()
            val username = bind.username.text.toString().trim()

            //checking if the input boxes are empty
            if (email.isNotEmpty() && pass.isNotEmpty() && checkPass.isNotEmpty() && username.isNotEmpty()) { // Check if all fields are filled
                //checking if password and checkpass are matching
                if (pass == checkPass) {
                    //using firebase function to sign up with email and password
                    auth.createUserWithEmailAndPassword(email, pass)
                        .addOnCompleteListener { task ->
                            //if sign up is successful proceed to input the data to database
                            if (task.isSuccessful) {
                                //get the new user user ID
                                val userId = FirebaseAuth.getInstance().currentUser?.uid
                                //check if it not null
                                if (userId != null) {
                                    // Create a user object
                                    val user = User(username, email)

                                    // Push the user object to Firebase Realtime Database
                                    FirebaseDatabase.getInstance().getReference("users").child(userId).setValue(user)

                                    //Pusing toast to the user to notify successful sign up process
                                    Toast.makeText(this, "Signed Up Successfully!", Toast.LENGTH_SHORT).show()
                                    //redirect the user to the sign in page
                                    val intent = Intent(this, SignInActivity::class.java).apply {
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                        //to indicate that the user has successfully signed up and going to the sign up activity
                                        putExtra("fromSignUp", true)
                                        // saving the user email in shared preferences for accessing their saved favourites later
                                        val sharedPref = getSharedPreferences("MyData", MODE_PRIVATE)
                                        val editor = sharedPref.edit()
                                        editor.putString("email", email)
                                        editor.apply()
                                    }
                                    startActivity(intent)
                                    finish()
                                    //if the user id is null then there is an issue with firebase and send a toast
                                } else {
                                    Toast.makeText(this, "User ID is null", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                //if the password is weak and less than 6 character and the account is already created then send a toast to the user
                                val message = when (task.exception) {
                                    is FirebaseAuthWeakPasswordException -> "Password is too weak."
                                    is FirebaseAuthUserCollisionException -> "An account already exists with this email."
                                    //otherwise send a sign up failed toast
                                    else -> "Sign up failed."
                                }
                                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    //if the password does not match send a toast to the user
                } else {
                    Toast.makeText(this, "Passwords don't match.", Toast.LENGTH_SHORT).show()
                }
                //if the user did not fill all of the fields send a toast to the user
            } else {
                Toast.makeText(this, "All fields are required.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}