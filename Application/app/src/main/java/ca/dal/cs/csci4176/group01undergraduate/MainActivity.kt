package ca.dal.cs.csci4176.group01undergraduate

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import ca.dal.cs.csci4176.group01undergraduate.ui.theme.Group01UndergraduateTheme
import com.google.firebase.auth.FirebaseAuth


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Check if user is signed in (non-null) and update UI accordingly.
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            // No user is signed in, so let's show the SignInActivity
            startActivity(Intent(this, SignInActivity::class.java))
            finish() // Finish MainActivity so the user can't navigate back to it
        } else {
            // The user is signed in, so let's proceed with MainActivity's content
            setContent {
                Group01UndergraduateTheme {
                    // A surface container using the 'background' color from the theme
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                    }
                }
            }
        }
    }
}
