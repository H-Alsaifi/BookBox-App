package ca.dal.cs.csci4176.group01undergraduate

import com.google.firebase.auth.FirebaseUser


sealed class SignInState {
    data object Idle : SignInState()
    data object Loading : SignInState()
    data class Success(val user: FirebaseUser) : SignInState()
    data class Error(val error: String) : SignInState()
}