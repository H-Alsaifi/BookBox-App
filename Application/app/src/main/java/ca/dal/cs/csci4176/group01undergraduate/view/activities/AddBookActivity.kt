package ca.dal.cs.csci4176.group01undergraduate.view.activities

//importing the needed libraries
import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import ca.dal.cs.csci4176.group01undergraduate.R
import ca.dal.cs.csci4176.group01undergraduate.model.Book
import ca.dal.cs.csci4176.group01undergraduate.repository.BookRepository
import ca.dal.cs.csci4176.group01undergraduate.state.BookState
import ca.dal.cs.csci4176.group01undergraduate.viewmodel.BookViewModel
import ca.dal.cs.csci4176.group01undergraduate.databinding.ActivityAddBookBinding
import ca.dal.cs.csci4176.group01undergraduate.intent.BookIntent
import ca.dal.cs.csci4176.group01undergraduate.viewmodel.factory.BookViewModelFactory
import com.google.zxing.integration.android.IntentIntegrator

//a class to add books to the database
class AddBookActivity : AppCompatActivity() {

    //initializing variables for binding the activity and the view model
    private lateinit var binding: ActivityAddBookBinding
    private val viewModel: BookViewModel by viewModels {
        BookViewModelFactory(BookRepository())
    }

    //variable for the latest fetched book
    private var lastFetchedBook: Book? = null

    /**
     * a method to be called when a new activity is created
     * @param savedInstanceState getting the saved state of the instance
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddBookBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        //setting the UI buttons and page
        setupObservers()
        setupSearchButton()
        setupScanButton()
        setupAddBookButton()
        setupManualButton()
    }

    //method to set the addbookbutton
    private fun setupAddBookButton() {
        binding.btnAddBook.setOnClickListener {
            lastFetchedBook?.let { book ->
                //show the progress of adding
                showLoading(true)
                //getting the book box ID from the intent
                val bookBoxKey = intent.getStringExtra("BOOK_BOX_KEY")
                //toast to the user the id of the box
                Log.d("AddBookActivity", "Received book box ID: $bookBoxKey")
                //when adding send the information of the book to the database
                viewModel.addBookToFirebase(this, book, bookBoxKey) { isSuccess, error ->
                    showLoading(false)
                    //if adding is successfull then add it to the database
                    if (isSuccess) {
                        displayMessage("Book added successfully")
                        restartActivity()
                        //otherwise send an error
                    } else {
                        displayMessage("Failed to add book: $error")
                    }
                }
            }
        }
    }

    /**
     * a method to show the loading progress
     * @param show showing the progress of the loading bar
     */
    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    /**
     * a method to restart the activity of adding the book
     */
    private fun restartActivity() {
        val intent = Intent(this, AddBookActivity::class.java)
        startActivity(intent)
    }

    /**
     * a method to setup the scan button by the camera
     */
    private fun setupScanButton() {
        binding.btnScanISBN.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
                //if camera permission is not granted then request it
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            } else {
                //otherwise scan the bar code of the book
                initiateScan()
            }
        }
    }

    /**
     * a method to request the camera permission from the user
     */
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                initiateScan()
            } else {
                Toast.makeText(this, "Camera permission is required to scan ISBN codes.", Toast.LENGTH_SHORT).show()
            }
        }

    //starting the scan of the barcode
    private fun initiateScan() {
        IntentIntegrator(this).apply{
            setBarcodeImageEnabled(true) // Capture the barcode image
            initiateScan()
        }
    }

    /**
     * a method to setup an observer for the ViewModel
     */
    @SuppressLint("SetTextI18n")
    private fun setupObservers() {
        viewModel.state.observe(this) { state ->
            when (state) {
                is BookState.Loading -> {
                    //displaying the book information as loading
                    binding.tvBookTitle.text = "Loading..."
                    binding.tvBookAuthor.text = ""
                    binding.tvBookISBN.text = ""
                    binding.tvBookDescription.text = ""
                    binding.tvBookRating.text = ""
                }
                //on success show the information of the book
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
                //otherwise send an error without showing anything
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

    /**
     * a method to enter the book information manually
     */
    private fun showManualEntryDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_manual_book_entry, null)
        val dialogBuilder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("Enter Book Details")
            .setPositiveButton("Add", null)
            .setNegativeButton("Cancel", null)

        val alertDialog = dialogBuilder.create()

        alertDialog.setOnShowListener {
            val button = (alertDialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)
            button.setOnClickListener {
                // Collect the entered details from the user
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

    /**
     * setting the search button when pressed on to enter the ISBN
     */
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

    //setting up the manual button
    private fun setupManualButton() {
        binding.btnManualEntry.setOnClickListener {
            showManualEntryDialog()
        }
    }

    /**
     * a method to handle the result when scanning with barcode
     * @param requestCode value that identifies the specific request made
     * @param resultCode value that indicates the result of the activity operation requested by the request code.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        //check if the result is null or not
        if (result != null) {
            if (result.contents == null) {
                //send a toast if scanning is cancelled
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show()
            } else {
                //otherwise show the scanned content in a toast
                Toast.makeText(this, "Scanned: " + result.contents, Toast.LENGTH_LONG).show()
                // Here, you can automatically fill in the ISBN field and initiate a search
                binding.editTextISBN.setText(result.contents)
                // Optionally, automatically search for the ISBN after scanning
                viewModel.processIntent(BookIntent.SearchBookByISBN(result.contents))
            }
            //take the result to a superclass if it is not done
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    /**
     * a method to show an alert to the user as a alert dialog
     * @param message the message to be shown to the user
     */
    private fun displayMessage(message: String) {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(message)
        builder.setTitle("Update !")
        builder.setCancelable(false)
        val alertDialog = builder.create()
        alertDialog.show()
        //dismiss the alert dialog after showing it for 5 seconds
        Handler(Looper.getMainLooper()).postDelayed({
            alertDialog.dismiss()
        }, 5000)
    }
}