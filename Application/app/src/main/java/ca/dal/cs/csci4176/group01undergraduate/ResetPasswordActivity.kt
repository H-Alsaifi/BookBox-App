package ca.dal.cs.csci4176.group01undergraduate


import android.graphics.PixelFormat
import android.os.Handler
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import ca.dal.cs.csci4176.group01undergraduate.databinding.ActivityResetPasswordBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ResetPasswordActivity : AppCompatActivity() {

//    private val CHANNEL_ID = "PasswordChangeChannel"
//    private val NOTIFICATION_ID = 12
    private lateinit var binding: ActivityResetPasswordBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResetPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.submitResetButton.setOnClickListener {
            val email = binding.emailEditText.text.toString().trim()
            if (email.isNotEmpty()) {
                FirebaseDatabase.getInstance().getReference("emails")
                    .orderByValue().equalTo(email).addListenerForSingleValueEvent(object :
                        ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                // Email exists, proceed to send reset link
                                auth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
//                                        sendPasswordChangeNotification(context = applicationContext)
                                        displayMessage("Reset link sent to your email.")
//                                        Toast.makeText(applicationContext, "Reset link sent to your email.", Toast.LENGTH_LONG).show()
                                        finish()
                                    } else {
                                        displayMessage("Failed to send reset link.")
//                                        Toast.makeText(applicationContext, "Failed to send reset link.", Toast.LENGTH_LONG).show()
                                    }
                                }
                            } else {
                                // Email not registered
                                displayMessage("Email not recognized.")
//                                Toast.makeText(applicationContext, "Email not recognized.", Toast.LENGTH_LONG).show()
                            }
                        }

                        private fun sendPasswordChangeNotification() {
                            val currentUser = auth.currentUser
                            if (currentUser != null) {
                                displayMessage("Your password has been changed.")
//                                Toast.makeText(applicationContext, "Your password has been changed.", Toast.LENGTH_LONG).show()
                            } else {
                                displayMessage("Failed to send password change notification: User not authenticated.")
//                                Toast.makeText(applicationContext, "Failed to send password change notification: User not authenticated.", Toast.LENGTH_LONG).show()
                            }
                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                            // Handle possible errors
                            Toast.makeText(applicationContext, "Error: ${databaseError.message}", Toast.LENGTH_LONG).show()
                        }
                    })
            } else {
                displayMessage("Please enter your email.")
//                Toast.makeText(applicationContext, "Please enter your email.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun displayMessage(message: String) {
        val messageView = layoutInflater.inflate(R.layout.display_message, null) as TextView
        messageView.text = message

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.CENTER

        val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        windowManager.addView(messageView, params)

        Handler().postDelayed({
            windowManager.removeView(messageView)
        }, 2000)
    }
//    @SuppressLint("MissingPermission")
//    fun sendPasswordChangeNotification(context: Context) {
//        createNotificationChannel(context)
//
//        val intent = Intent(context, ResetPasswordActivity::class.java)
//        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_MUTABLE)
//
//        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
//            .setSmallIcon(R.drawable.ic_notification_icon)
//            .setContentTitle("Password Changed")
//            .setContentText("Your password has been changed successfully.")
//            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//            .setContentIntent(pendingIntent)
//            .setAutoCancel(true)
//
//        with(NotificationManagerCompat.from(context)) {
//            notify(NOTIFICATION_ID, notificationBuilder.build())
//        }
//    }
//    private fun createNotificationChannel(context: Context) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val name = "Password Change Notifications"
//            val descriptionText = "Notifications for password changes"
//            val importance = NotificationManager.IMPORTANCE_DEFAULT
//            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
//                description = descriptionText
//            }
//            val notificationManager: NotificationManager =
//                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//            notificationManager.createNotificationChannel(channel)
//        }
//    }
}
