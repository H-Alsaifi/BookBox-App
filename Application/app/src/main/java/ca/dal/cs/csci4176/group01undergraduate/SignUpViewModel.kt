package ca.dal.cs.csci4176.group01undergraduate

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

class SignUpViewModel(private val auth: FirebaseAuth) : ViewModel() {
    private val _signUpState = MutableLiveData<SignUpState>()
    val signUpState: LiveData<SignUpState> get() = _signUpState

    fun processIntent(intent: SignUpIntent) {
        when (intent) {
            is SignUpIntent.SignUp -> performSignUp(intent.email, intent.password)
        }
    }

    private fun performSignUp(email: String, password: String) {
        _signUpState.value = SignUpState.Loading
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                _signUpState.value = SignUpState.Success("Signed Up Successfully!")
            }
            .addOnFailureListener {
                _signUpState.value = SignUpState.Error(it.message ?: "Sign up failed.")
            }
    }
}
