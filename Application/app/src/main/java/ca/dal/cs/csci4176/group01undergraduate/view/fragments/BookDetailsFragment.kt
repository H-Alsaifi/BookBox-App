package ca.dal.cs.csci4176.group01undergraduate.view.fragments
//importing the needed libraries
import android.annotation.SuppressLint
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
import ca.dal.cs.csci4176.group01undergraduate.R
import ca.dal.cs.csci4176.group01undergraduate.model.Book
import ca.dal.cs.csci4176.group01undergraduate.repository.BookRepository
import ca.dal.cs.csci4176.group01undergraduate.viewmodel.BookViewModel
import ca.dal.cs.csci4176.group01undergraduate.viewmodel.factory.BookViewModelFactory
import com.google.firebase.database.FirebaseDatabase

//fragment to display book details
class BookDetailsFragment : Fragment() {

    //companion for the book object
    companion object {
        private const val ARG_BOOK = "book"
        //creating a new method to a new instance of bookdetails fragments
        fun newInstance(book: Book): BookDetailsFragment {
            val args = Bundle().apply {
                putParcelable(ARG_BOOK, book)
            }
            return BookDetailsFragment().apply {
                arguments = args
            }
        }
    }

    //initializing variables for book and the view model
    private var book: Book? = null
    private lateinit var viewModel: BookViewModel
    //a oncreateview method to be called when fragment UI is created
    @SuppressLint("CutPasteId")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        //initializing variable for the view of book fragment xml
        val view = inflater.inflate(R.layout.fragment_book_details, container, false)
        //initializing variable forthe book object from the argument
        val book = arguments?.getParcelable<Book>(ARG_BOOK)
        // Initialize the BookRepository
        val bookRepository = BookRepository()

        // Create an instance of the ViewModelFactory
        val viewModelFactory = BookViewModelFactory(bookRepository)

        // Obtain the ViewModel
        viewModel = ViewModelProvider(this, viewModelFactory)[BookViewModel::class.java]

        //setting the book variables with its buttons and details
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

        //a button for the rating of the book
        val btnRateBook = view.findViewById<Button>(R.id.btnRateBook)
        btnRateBook.setOnClickListener {
            book?.let { book ->
                showRatingDialog(book)
            }
        }
        //return the view and the book
        return view
    }

    /**
     * a method to display the rating of the book
     * @param book book which will be rated
     */
    private fun showRatingDialog(book: Book) {
        //variables for the dialogview and rating bar of the book
        val dialogView = layoutInflater.inflate(R.layout.rate_book_dialog, null)
        val ratingBar = dialogView.findViewById<RatingBar>(R.id.dialogRatingBar)

        //an alert dialog to display a rating dislog
        AlertDialog.Builder(requireContext())
            //setting rating for each book
            .setTitle("Rate: ${book.title}")
            //setting view for dialog view
            .setView(dialogView)
            .setPositiveButton("Rate") { dialog, _ ->
                //new rating of the book to be shown when the button is pressed on
                val newRating = ratingBar.rating
                //call of function when pressed on
                updateBookRating(book, newRating)
                //dismiss the dialog after rating
                dialog.dismiss()
            }
                //cancel rating of the book
            .setNegativeButton("Cancel", null)
            .show()
    }

    /**
     * a method to update the rating of the book
     * @param book the book which will have the new rating
     * @param userRating the rating of each user
     */
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

        //a map of the updates that will be done and pushed to the database
        val updates = hashMapOf<String, Any>(
            "rating" to newAverageRating,
            "totalRatings" to newTotalRatings
        )
        //updating the old information in the database to the new one
        bookRef.updateChildren(updates).addOnCompleteListener { task ->
            //check if it successfully done then display a toast with the new rating
            if (task.isSuccessful) {
                val formattedRating = String.format("%.1f", newAverageRating)
                Toast.makeText(context, "Rating updated successfully to $formattedRating", Toast.LENGTH_SHORT).show()
            }
            //else send a toast with an exception or issue
            else {
                Toast.makeText(context, "Failed to update rating", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

