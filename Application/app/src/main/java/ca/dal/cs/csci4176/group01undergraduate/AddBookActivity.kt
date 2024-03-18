package ca.dal.cs.csci4176.group01undergraduate

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddBookBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        setupObservers()
        setupSearchButton()
        setupScanButton()
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
            setOrientationLocked(false) // lock orientation or not
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
                    binding.txtBookDetails.text = "Title: ${book.title}\nAuthor: ${book.author}\nISBN: ${book.isbn}\nDescription: ${book.description}\nRating: ${book.rating}"
                }
                is BookState.Error -> binding.txtBookDetails.text = "Error: ${state.error}"
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

}