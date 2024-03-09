package ca.dal.cs.csci4176.group01undergraduate

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import ca.dal.cs.csci4176.group01undergraduate.databinding.ActivitySignInBinding
import com.google.firebase.auth.FirebaseAuth

class SignInActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignInBinding
    private val viewModel: SignInViewModel by viewModels { ViewModelFactory(FirebaseAuth.getInstance()) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.signInState.observe(this) { state ->
            when (state) {
                is SignInState.Idle -> handleIdleState()
                is SignInState.Loading -> showLoading()
                is SignInState.Success -> handleSuccess()
                is SignInState.Error -> showError(state.error)
            }
        }

        binding.SigninButton.setOnClickListener {
            val email = binding.emailEt.text.toString().trim()
            val password = binding.passET.text.toString().trim()
            if (email.isNotEmpty() && password.isNotEmpty()) {
                viewModel.handleIntent(SignInIntent.SignIn(email, password))
            } else {
                showError("Please enter both email and password.")
            }
        }
    }

    private fun handleIdleState() {
        binding.progressBar.visibility = View.GONE
        binding.SigninButton.isEnabled = true
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.SigninButton.isEnabled = false
    }

    private fun handleSuccess() {
        Toast.makeText(this, "Sign-in successful!", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun showError(error: String) {
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
    }
}
