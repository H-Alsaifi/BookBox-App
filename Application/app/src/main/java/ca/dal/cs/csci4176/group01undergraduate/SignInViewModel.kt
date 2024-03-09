package ca.dal.cs.csci4176.group01undergraduate

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

class SignInViewModel(private val auth: FirebaseAuth) : ViewModel() {
    private val _signInState = MutableLiveData<SignInState>().apply { value = SignInState.Idle }
    val signInState: LiveData<SignInState> = _signInState

    fun handleIntent(intent: SignInIntent) {
        when (intent) {
            is SignInIntent.SignIn -> signInUser(intent.email, intent.password)
        }
    }

    private fun signInUser(email: String, password: String) {
        _signInState.value = SignInState.Loading
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful && task.result?.user != null) {
                    _signInState.value = SignInState.Success(task.result.user!!)
                } else {
                    _signInState.value = SignInState.Error(task.exception?.message ?: "Sign in error")
                }
            }
    }
}
