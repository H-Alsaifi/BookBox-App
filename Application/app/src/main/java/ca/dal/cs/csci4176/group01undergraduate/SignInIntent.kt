package ca.dal.cs.csci4176.group01undergraduate

sealed class SignInIntent {
    data class SignIn(val email: String,
                      val password: String)
        :SignInIntent()
}