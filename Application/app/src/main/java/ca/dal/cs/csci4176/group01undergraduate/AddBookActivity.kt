package ca.dal.cs.csci4176.group01undergraduate

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.PixelFormat
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.WindowManager
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
    }

    private fun setupAddBookButton() {
        binding.btnAddBook.setOnClickListener {
            lastFetchedBook?.let { book ->
                showLoading(true)
                val bookBoxKey = intent.getStringExtra("BOOK_BOX_KEY")
                Log.d("AddBookActivity", "Received book box ID: $bookBoxKey")
                viewModel.addBookToFirebase(book, bookBoxKey) { isSuccess, error ->
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
        val builder = AlertDialog.Builder(this)
        builder.setMessage(message)
        builder.setTitle("Update !")
        builder.setCancelable(false)
        val alertDialog = builder.create()
        alertDialog.show()
        Handler(Looper.getMainLooper()).postDelayed({
            alertDialog.dismiss()
        }, 5000)

    }



}