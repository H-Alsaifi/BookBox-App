package ca.dal.cs.csci4176.group01undergraduate

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.PixelFormat
import android.text.Html
import android.os.Bundle
import android.util.Log
import android.os.Handler
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
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
        setupManualButton()
    }

    private fun setupAddBookButton() {
        binding.btnAddBook.setOnClickListener {
            lastFetchedBook?.let { book ->
                showLoading(true)
                val bookBoxKey = intent.getStringExtra("BOOK_BOX_KEY")
                Log.d("AddBookActivity", "Received book box ID: $bookBoxKey")
                viewModel.addBookToFirebase(this, book, bookBoxKey) { isSuccess, error ->
                    showLoading(false)
                    if (isSuccess) {
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
                Toast.makeText(this, "Camera permission is required to scan ISBN codes.", Toast.LENGTH_SHORT).show()
            }
        }

    private fun initiateScan() {
        IntentIntegrator(this).apply{
            setBarcodeImageEnabled(true) // Capture the barcode image
            initiateScan()
        }
    }

    private fun setupObservers() {
        viewModel.state.observe(this) { state ->
            when (state) {
                is BookState.Loading -> {
                    binding.tvBookTitle.text = "Loading..."
                    binding.tvBookAuthor.text = ""
                    binding.tvBookISBN.text = ""
                    binding.tvBookDescription.text = ""
                    binding.tvBookRating.text = ""
                }
                is BookState.Success -> {
                    val book = state.book
                    lastFetchedBook = book // Save the fetched book
                    binding.tvBookTitle.text = "Title: ${book.title}"
                    binding.tvBookAuthor.text = "Author: ${book.author}"
                    binding.tvBookISBN.text = "ISBN: ${book.isbn}"
                    binding.tvBookDescription.text = "Description: ${book.description}"
                    binding.tvBookRating.text = "Rating: ${book.rating}"
                    binding.btnAddBook.visibility = View.VISIBLE // Show the "Add Book" button
                }
                is BookState.Error -> {
                    binding.tvBookTitle.text = "Error: ${state.error}"
                    binding.tvBookAuthor.text = ""
                    binding.tvBookISBN.text = ""
                    binding.tvBookDescription.text = ""
                    binding.tvBookRating.text = ""
                    binding.btnAddBook.visibility = View.GONE // Hide the "Add Book" button
                }
                else -> {}
            }
        }
    }

    private fun showManualEntryDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_manual_book_entry, null)
        val dialogBuilder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("Enter Book Details")
            .setPositiveButton("Add", null) // We will override the onClickListener
            .setNegativeButton("Cancel", null)

        val alertDialog = dialogBuilder.create()

        alertDialog.setOnShowListener {
            val button = (alertDialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)
            button.setOnClickListener {
                // Collect the entered details
                val title = dialogView.findViewById<EditText>(R.id.manualTitle).text.toString()
                val author = dialogView.findViewById<EditText>(R.id.manualAuthor).text.toString()
                val description = dialogView.findViewById<EditText>(R.id.manualDescription).text.toString()
                val rating = dialogView.findViewById<EditText>(R.id.manualRating).text.toString().toDoubleOrNull() ?: 0.0

                // Prepare a Book object
                val book = Book(title, author, "0", description, rating, 0, "", "")
                lastFetchedBook = book // Set the lastFetchedBook to the manually entered book

                // Attempt to add the book to Firebase
                val bookBoxKey = intent.getStringExtra("BOOK_BOX_KEY")
                viewModel.addBookToFirebase(this, book, bookBoxKey) { isSuccess, error ->
                    if (isSuccess) {
                        displayMessage("Book added successfully")
                        alertDialog.dismiss()
                    } else {
                        displayMessage("Failed to add book: $error")
                    }
                }
            }
        }

        alertDialog.show()
    }


//    private fun setupObservers() {
//        viewModel.state.observe(this) { state ->
//            when (state) {
//                is BookState.Loading -> binding.txtBookDetails.text = "Loading..."
//                is BookState.Success -> {
//                    val book = state.book
//                    lastFetchedBook = book // Save the fetched book
//                    val formattedDetails = "<b>Title:</b> ${book.title}<br/>" +
//                    "<b>Author:</b> ${book.author}<br/>" +
//                            "<b>ISBN:</b> ${book.isbn}<br/>" +
//                            "<b>Description:</b> ${book.description}<br/>" +
//                            "<b>Rating:</b> ${book.rating}"
//                    binding.txtBookDetails.text = Html.fromHtml(formattedDetails, Html.FROM_HTML_MODE_LEGACY)
//                    binding.btnAddBook.visibility = View.VISIBLE // Show the "Add Book" button
//                }
//
//                is BookState.Error -> {
//                    binding.txtBookDetails.text = "Error: ${state.error}"
//                    binding.btnAddBook.visibility = View.GONE // Hide the "Add Book" button
//                }
//
//                else -> {}
//            }
//        }
//    }

    private fun setupSearchButton() {
        binding.btnSearch.setOnClickListener {
            val isbn = binding.editTextISBN.text.toString()
            if (isbn.isNotEmpty()) {
                viewModel.processIntent(BookIntent.SearchBookByISBN(isbn))
            } else {
                binding.tvBookTitle.text = "Please enter an ISBN."
            }
        }
    }

    private fun setupManualButton() {
        binding.btnManualEntry.setOnClickListener {
            showManualEntryDialog()
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



}