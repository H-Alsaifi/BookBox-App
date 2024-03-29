package ca.dal.cs.csci4176.group01undergraduate.view.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ca.dal.cs.csci4176.group01undergraduate.R
import ca.dal.cs.csci4176.group01undergraduate.model.Book
import ca.dal.cs.csci4176.group01undergraduate.view.adapters.BooksAdapter
import com.google.firebase.database.*

class SearchFragment : Fragment() {

    private lateinit var booksAdapter: BooksAdapter
    private lateinit var databaseReference: DatabaseReference
    private val booksList = mutableListOf<Book>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_search_books, container, false)

        databaseReference = FirebaseDatabase.getInstance().reference.child("Books")
        val searchQueryEditText: EditText = view.findViewById(R.id.searchQueryEditText)
        val searchByNameCheckbox: CheckBox = view.findViewById(R.id.searchByNameCheckbox)
        val searchByAuthorCheckbox: CheckBox = view.findViewById(R.id.searchByAuthorCheckbox)
        val searchButton: Button = view.findViewById(R.id.searchButton)
        val booksRecyclerView: RecyclerView = view.findViewById(R.id.booksRecyclerView)
        booksRecyclerView.layoutManager = LinearLayoutManager(requireContext())
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

        searchButton.setOnClickListener {
            val queryText = searchQueryEditText.text.toString()
            val searchByName = searchByNameCheckbox.isChecked
            val searchByAuthor = searchByAuthorCheckbox.isChecked
            searchBooks(queryText, searchByName, searchByAuthor)
        }
        return view
    }
    private fun searchBooks(queryText: String, searchByName: Boolean, searchByAuthor: Boolean) {
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                booksList.clear()
                for (bookSnapshot in snapshot.children) {
                    val book = bookSnapshot.getValue(Book::class.java)
                    if (book != null) {
                        if (searchByName && book.title.contains(queryText, ignoreCase = true)) {
                            booksList.add(book)
                        } else if (searchByAuthor && book.author.contains(queryText, ignoreCase = true)) {
                            booksList.add(book)
                        }
                    }
                }
                booksAdapter.notifyDataSetChanged()
            }
            override fun onCancelled(error: DatabaseError) {
                // Handle database error
            }
        })
    }
}
