//package ca.dal.cs.csci4176.group01undergraduate
//
//import android.content.Context
//import android.content.Intent
//import android.os.Bundle
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import ca.dal.cs.csci4176.group01undergraduate.databinding.ActivitySignInBinding
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
//import com.google.firebase.auth.FirebaseAuthInvalidUserException
//
//class SignIn : AppCompatActivity() {
//
//    private lateinit var auth: FirebaseAuth
//    private lateinit var bind: ActivitySignInBinding
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        bind = ActivitySignInBinding.inflate(layoutInflater)
//        setContentView(bind.root)
//
//        auth = FirebaseAuth.getInstance()
//
//        bind.textView.setOnClickListener {
//            val intent = Intent(this, SignUp::class.java)
//            startActivity(intent)
//        }
//
//        bind.SigninButton.setOnClickListener {
//            val email = bind.emailEt.text.toString()
//            val pass = bind.passET.text.toString()
//
//            if (email.isNotEmpty() && pass.isNotEmpty()) {
//                auth.signInWithEmailAndPassword(email, pass).addOnCompleteListener { task->
//                    if (task.isSuccessful) {
//                        val staySign = bind.staySignedInButton.isChecked
//                        getSharedPreferences("prefs", Context.MODE_PRIVATE)
//                            .edit().putBoolean("staySign", staySign).apply()
//
//                        val intent = Intent(this, MainActivity::class.java)
//                        startActivity(intent)
//                        finish()
//                    } else {
//                        val message = when (task.exception) {
//                            is FirebaseAuthInvalidCredentialsException -> "Invalid email or password."
//                            is FirebaseAuthInvalidUserException -> "No account found with this email."
//                            else -> "Invalid Credentials!"
//                        }
//                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
//                    }
//                }
//            } else {
//                Toast.makeText(this, "Please fill the fields!", Toast.LENGTH_SHORT).show()
//            }
//        }
//    }
//
//    override fun onStart() {
//        super.onStart()
//
//        val staySign = getSharedPreferences("prefs", Context.MODE_PRIVATE)
//            .getBoolean("staySign", false)
//
//        if(auth.currentUser != null && !checkSignUp() && staySign){
//            val intent = Intent(this, MainActivity::class.java)
//            startActivity(intent)
//            finish()
//        }
//    }
//    private fun checkSignUp(): Boolean{
//        val signUp = intent.getBooleanExtra("fromSignUp", false)
//        intent.removeExtra("fromSignUp")
//        return signUp
//    }
//}