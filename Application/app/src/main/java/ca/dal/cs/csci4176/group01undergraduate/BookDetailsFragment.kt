package ca.dal.cs.csci4176.group01undergraduate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import ca.dal.cs.csci4176.group01undergraduate.addBookISBN.Book

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


//    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
//        val view = inflater.inflate(R.layout.fragment_book_details, container, false)
//        val book = arguments?.getParcelable<Book>(ARG_BOOK)
//
//        view.findViewById<TextView>(R.id.bookDetailsTitle).text = book?.title
//        view.findViewById<TextView>(R.id.bookDetailsAuthor).text = book?.author
//        view.findViewById<TextView>(R.id.bookDetailsDescription).text = book?.description
//        view.findViewById<TextView>(R.id.bookDetailsRating).text = book?.rating.toString()
//
//        return view
//    }
override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    val view = inflater.inflate(R.layout.fragment_book_details, container, false)
    val book = arguments?.getParcelable<Book>(ARG_BOOK)

    book?.let {
        view.findViewById<TextView>(R.id.bookDetailsTitle).text = getString(R.string.book_details_title, it.title)
        view.findViewById<TextView>(R.id.bookDetailsAuthor).text = getString(R.string.book_details_author, it.author)
        view.findViewById<TextView>(R.id.bookDetailsDescription).text = getString(R.string.book_details_description, it.description)
        view.findViewById<TextView>(R.id.bookDetailsRating).text = getString(R.string.book_details_rating, it.rating)
    }

    return view
}


}

