package ca.dal.cs.csci4176.group01undergraduate

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import ca.dal.cs.csci4176.group01undergraduate.databinding.ActivitySignUpBinding

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private val signUpViewModel: SignUpViewModel by viewModels {
        ViewModelProvider.AndroidViewModelFactory.getInstance(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        signUpViewModel.signUpState.observe(this, Observer { state ->
            when (state) {
                is SignUpState.Idle -> handleIdleState()
                is SignUpState.Loading -> showLoading()
                is SignUpState.Success -> handleSuccess(state.message)
                is SignUpState.Error -> showError(state.error)
            }
        })

        binding.signupButton.setOnClickListener {
            val email = binding.email.text.toString().trim()
            val pass = binding.pass.text.toString().trim()
            val confirmPass = binding.confirmPass.text.toString().trim()

            if (email.isNotEmpty() && pass.isNotEmpty() && pass == confirmPass) {
                signUpViewModel.processIntent(SignUpIntent.SignUp(email, pass))
            } else {
                Toast.makeText(this, "Please check your input", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleIdleState() {
        binding.progressBar.visibility = View.GONE
        binding.signupButton.isEnabled = true
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.signupButton.isEnabled = false
    }

    private fun handleSuccess(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showError(error: String) {
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
        binding.progressBar.visibility = View.GONE
        binding.signupButton.isEnabled = true
    }
}
