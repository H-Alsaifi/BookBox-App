package ca.dal.cs.csci4176.group01undergraduate

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.PixelFormat
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import ca.dal.cs.csci4176.group01undergraduate.addBookISBN.Book
import ca.dal.cs.csci4176.group01undergraduate.addBookISBN.BookRepository
import ca.dal.cs.csci4176.group01undergraduate.addBookISBN.BookState
import ca.dal.cs.csci4176.group01undergraduate.addBookISBN.BookViewModel
import ca.dal.cs.csci4176.group01undergraduate.databinding.ActivityAddBookBinding
import ca.dal.cs.csci4176.group01undergraduate.addBookISBN.BookIntent
import ca.dal.cs.csci4176.group01undergraduate.addBookISBN.BookViewModelFactory
import com.google.zxing.integration.android.IntentIntegrator

class AddBookActivity : AppCompatActivity() {
//
//    private val CHANNEL_ID = "BookAddedChannel"
//    private val NOTIFICATION_ID = 12

    private lateinit var binding: ActivityAddBookBinding
    private val viewModel: BookViewModel by viewModels {
        BookViewModelFactory(BookRepository())
    }

    private var lastFetchedBook: Book? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddBookBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        setupObservers()
        setupSearchButton()
        setupScanButton()
        setupAddBookButton()
    }

    private fun setupAddBookButton() {
        binding.btnAddBook.setOnClickListener {
            lastFetchedBook?.let { book ->
                showLoading(true)
                viewModel.addBookToFirebase(book) { isSuccess, error ->
                    showLoading(false)
                    if (isSuccess) {
//                        sendBookAddedNotification(context = applicationContext)
                        displayMessage("Book added successfully")
//                        Toast.makeText(this@AddBookActivity, "Book added successfully", Toast.LENGTH_SHORT).show()
                        restartActivity()
                    } else {
                        displayMessage("Failed to add book: $error")
//                        Toast.makeText(this@AddBookActivity, "Failed to add book: $error", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun restartActivity() {
        val intent = Intent(this, AddBookActivity::class.java)
        startActivity(intent)
        finish()
    }


    private fun setupScanButton() {
        binding.btnScanISBN.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            } else {
                initiateScan()
            }
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                initiateScan()
            } else {
                displayMessage("Camera permission is required to scan ISBN codes.")
//                Toast.makeText(this, "Camera permission is required to scan ISBN codes.", Toast.LENGTH_SHORT).show()
            }
        }

    private fun initiateScan() {
        IntentIntegrator(this).apply{
            setBarcodeImageEnabled(true) // Capture the barcode image
            initiateScan()
        }
    }

    private fun setupObservers() {
        viewModel.state.observe(this, { state ->
            when (state) {
                is BookState.Loading -> binding.txtBookDetails.text = "Loading..."
                is BookState.Success -> {
                    val book = state.book
                    lastFetchedBook = book // Save the fetched book
                    binding.txtBookDetails.text = "Title: ${book.title}\nAuthor: ${book.author}\nISBN: ${book.isbn}\nDescription: ${book.description}\nRating: ${book.rating}"
                    binding.btnAddBook.visibility = View.VISIBLE // Show the "Add Book" button
                }
                is BookState.Error -> {
                    binding.txtBookDetails.text = "Error: ${state.error}"
                    binding.btnAddBook.visibility = View.GONE // Hide the "Add Book" button
                }
                else -> {}
            }
        })
    }

    private fun setupSearchButton() {
        binding.btnSearch.setOnClickListener {
            val isbn = binding.editTextISBN.text.toString()
            if (isbn.isNotEmpty()) {
                viewModel.processIntent(BookIntent.SearchBookByISBN(isbn))
            } else {
                binding.txtBookDetails.text = "Please enter an ISBN."
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Scanned: " + result.contents, Toast.LENGTH_LONG).show()
                // Here, you can automatically fill in the ISBN field and initiate a search
                binding.editTextISBN.setText(result.contents)
                // Optionally, automatically search for the ISBN after scanning
                viewModel.processIntent(BookIntent.SearchBookByISBN(result.contents))
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
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

//
//    @SuppressLint("MissingPermission")
//    fun sendBookAddedNotification(context: Context) {
//        createNotificationChannel(context)
//
//        val intent = Intent(context, ResetPasswordActivity::class.java)
//        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_MUTABLE)
//
//        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
//            .setSmallIcon(R.drawable.ic_notification_icon)
//            .setContentTitle("Book Added")
//            .setContentText("Book has been Added successfully.")
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
//            val name = "Book added Notifications"
//            val descriptionText = "Notifications for Book Added"
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