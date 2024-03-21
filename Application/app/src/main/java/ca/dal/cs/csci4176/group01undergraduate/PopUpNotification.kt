package ca.dal.cs.csci4176.group01undergraduate

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging

class PopupNotification(private val context: Context) {

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()
    private val notificationDatabaseRef = database.getReference("notifications")

    fun authenticateUser(email: String, password: String, callback: (Boolean, String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    sendLoginNotification(email)
                    callback(true, null)
                } else {
                    callback(false, task.exception?.message ?: "Authentication failed")
                }
            }
    }

    fun sendPasswordChangeNotification() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            showToast("Your password has been changed.")
        } else {
            showToast("Failed to send password change notification: User not authenticated.")
        }
    }


    private fun getPasswordChangeNotificationMessage(email: String): String {
        return "Your password for $email has been changed."
    }

    fun subscribeToFavoriteBookBoxesUpdates(userId: String) {
        val userRef = database.getReference("users").child(userId)
        userRef.child("favoriteBookBoxes").addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val bookBoxId = snapshot.value.toString()
                val bookBoxRef = database.getReference("bookBoxes").child(bookBoxId)
                bookBoxRef.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val bookBox = snapshot.getValue(BookBox::class.java)
                        if (bookBox != null) {
                            sendFavoriteBookBoxUpdateNotification(userId, bookBox)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        showToast("Failed to subscribe to favorite Book Boxes updates: ${error.message}")
                    }
                })
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                // No action needed
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                // No action needed
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                // No action needed
            }

            override fun onCancelled(error: DatabaseError) {
                showToast("Failed to subscribe to favorite Book Boxes updates: ${error.message}")
            }
        })
    }

    private fun sendFavoriteBookBoxUpdateNotification(userId: String, bookBox: BookBox) {
        try {
            val notificationId = notificationDatabaseRef.push().key ?: throw Exception("Notification ID is null")
            val notification = Notification(userId, "New book added to ${bookBox.name}")
            notificationDatabaseRef.child(notificationId).setValue(notification)
        } catch (e: Exception) {
            showToast("Failed to send favorite Book Box update notification: ${e.message}")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}
