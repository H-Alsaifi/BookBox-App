package ca.dal.cs.csci4176.group01undergraduate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import ca.dal.cs.csci4176.group01undergraduate.addBookISBN.Book
import com.google.firebase.database.*

class BookListFragment : Fragment() {

    companion object {
        private const val ARG_BOOK_BOX_ID = "bookBoxId"

        fun newInstance(bookBoxId: String): BookListFragment {
            val args = Bundle()
            args.putString(ARG_BOOK_BOX_ID, bookBoxId)
            val fragment = BookListFragment()
            fragment.arguments = args
            return fragment
        }
    }

    private lateinit var booksAdapter: BooksAdapter
    private lateinit var databaseReference: DatabaseReference
    private val booksList = mutableListOf<Book>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_book_list, container, false)

        val bookBoxId = arguments?.getString(ARG_BOOK_BOX_ID) ?: return view
        databaseReference = FirebaseDatabase.getInstance().getReference("Books")

        val booksRecyclerView: RecyclerView = view.findViewById(R.id.rvBooks)
        booksAdapter = BooksAdapter(booksList) { selectedBook ->
            // Navigate to BookDetailsFragment with selectedBook details
            val fragment = BookDetailsFragment.newInstance(selectedBook)
            parentFragmentManager.beginTransaction().apply {
                replace(R.id.fragment_container, fragment)
                addToBackStack(null)
                commit()
            }
        }

        booksRecyclerView.adapter = booksAdapter

        fetchBooksForBookBox(bookBoxId)

        return view
    }

    private fun fetchBooksForBookBox(bookBoxId: String) {
        databaseReference.orderByChild("bookBoxID").equalTo(bookBoxId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    booksList.clear()
                    snapshot.children.mapNotNullTo(booksList) { it.getValue(Book::class.java) }
                    booksAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle possible errors
                }
            })
    }
}

