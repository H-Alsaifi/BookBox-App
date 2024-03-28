package ca.dal.cs.csci4176.group01undergraduate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import ca.dal.cs.csci4176.group01undergraduate.addBookISBN.Book
import ca.dal.cs.csci4176.group01undergraduate.addBookISBN.BookRepository
import ca.dal.cs.csci4176.group01undergraduate.addBookISBN.BookViewModel
import ca.dal.cs.csci4176.group01undergraduate.addBookISBN.BookViewModelFactory
import com.google.firebase.database.FirebaseDatabase

class BookDetailsFragment : Fragment() {

    companion object {
        private const val ARG_BOOK = "book"

        fun newInstance(book: Book): BookDetailsFragment {
            val args = Bundle().apply {
                putParcelable(ARG_BOOK, book)
            }
            return BookDetailsFragment().apply {
                arguments = args
            }
        }
    }

    private var book: Book? = null
    private lateinit var viewModel: BookViewModel
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_book_details, container, false)
        val book = arguments?.getParcelable<Book>(ARG_BOOK)
        // Initialize the BookRepository
        val bookRepository = BookRepository()

        // Create an instance of the ViewModelFactory
        val viewModelFactory = BookViewModelFactory(bookRepository)

        // Obtain the ViewModel
        viewModel = ViewModelProvider(this, viewModelFactory).get(BookViewModel::class.java)

        book?.let {
            val viewModel: BookViewModel by viewModels()
            viewModel.getBookLiveData(book.bookBoxID).observe(viewLifecycleOwner) { updatedBook ->
                // Update UI with updatedBook details
                view?.findViewById<TextView>(R.id.bookDetailsRating)?.text = getString(R.string.book_details_rating, updatedBook.rating)
            }
            view.findViewById<TextView>(R.id.bookDetailsTitle).text = getString(R.string.book_details_title, it.title)
            view.findViewById<TextView>(R.id.bookDetailsAuthor).text = getString(R.string.book_details_author, it.author)
            view.findViewById<TextView>(R.id.bookDetailsDescription).text = getString(R.string.book_details_description, it.description)
            view.findViewById<TextView>(R.id.bookDetailsRating).text = getString(R.string.book_details_rating, it.rating)
        }

        val btnRateBook = view.findViewById<Button>(R.id.btnRateBook)
        btnRateBook.setOnClickListener {
            book?.let { book ->
                showRatingDialog(book)
            }
        }


        return view
    }

    private fun showRatingDialog(book: Book) {
        val dialogView = layoutInflater.inflate(R.layout.rate_book_dialog, null)
        val ratingBar = dialogView.findViewById<RatingBar>(R.id.dialogRatingBar)

        AlertDialog.Builder(requireContext())
            .setTitle("Rate: ${book.title}")
            .setView(dialogView)
            .setPositiveButton("Rate") { dialog, _ ->
                val newRating = ratingBar.rating
                updateBookRating(book, newRating)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateBookRating(book: Book, userRating: Float) {
        // First, calculate the new average rating
        val totalRatingScore = book.rating * book.totalRatings
        val newTotalRatingScore = totalRatingScore + userRating
        val newTotalRatings = book.totalRatings + 1
        val newAverageRating = newTotalRatingScore / newTotalRatings

        // Update the book object (if you want to keep it updated locally)
        book.rating = newAverageRating
        book.totalRatings = newTotalRatings

        // Now, update the new rating and totalRatings in Firebase
        val bookRef = FirebaseDatabase.getInstance().getReference("Books").child(book.bookBoxID)

        val updates = hashMapOf<String, Any>(
            "rating" to newAverageRating,
            "totalRatings" to newTotalRatings
        )

        bookRef.updateChildren(updates).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val formattedRating = String.format("%.1f", newAverageRating)
                Toast.makeText(context, "Rating updated successfully to $formattedRating", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Failed to update rating", Toast.LENGTH_SHORT).show()
            }

        }
    }

}

