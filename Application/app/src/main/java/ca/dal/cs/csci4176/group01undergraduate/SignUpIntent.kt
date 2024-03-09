package ca.dal.cs.csci4176.group01undergraduate

sealed class SignUpIntent {
    data class SignUp(val email: String,
                      val password: String)
        :SignUpIntent()
}