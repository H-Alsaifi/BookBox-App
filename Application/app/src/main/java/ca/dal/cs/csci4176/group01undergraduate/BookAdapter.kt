package ca.dal.cs.csci4176.group01undergraduate

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ca.dal.cs.csci4176.group01undergraduate.addBookISBN.Book


//class BooksAdapter(private val booksList: List<Book>) : RecyclerView.Adapter<BooksAdapter.ViewHolder>() {
//
//    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
//        val addressTextView: TextView = view.findViewById(R.id.bookBoxAddressTextView)
//        val titleTextView: TextView = view.findViewById(R.id.bookTitleTextView)
//        val authorTextView: TextView = view.findViewById(R.id.bookAuthorTextView)
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
//        val view = LayoutInflater.from(parent.context).inflate(R.layout.book_item, parent, false)
//        return ViewHolder(view)
//    }
//
//    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        val book = booksList[position]
//        holder.titleTextView.text = "Title: ${book.title}"
//        holder.authorTextView.text = "Author: ${book.author}"
//        holder.addressTextView.text = "Location: ${book.address}"
//    }
//
//    override fun getItemCount() = booksList.size
//
//}

class BooksAdapter(private val booksList: List<Book>, private val onClick: (Book) -> Unit) : RecyclerView.Adapter<BooksAdapter.ViewHolder>() {

    class ViewHolder(view: View, val onClick: (Book) -> Unit) : RecyclerView.ViewHolder(view) {
        val addressTextView: TextView = view.findViewById(R.id.bookBoxAddressTextView)
        val titleTextView: TextView = view.findViewById(R.id.bookTitleTextView)
        val authorTextView: TextView = view.findViewById(R.id.bookAuthorTextView)
        private var currentBook: Book? = null

        init {
            view.setOnClickListener {
                currentBook?.let {
                    onClick(it)
                }
            }
        }

        fun bind(book: Book) {
            currentBook = book
            titleTextView.text = "Title: ${book.title}"
            authorTextView.text = "Author: ${book.author}"
            addressTextView.text = "Location: ${book.address}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.book_item, parent, false)
        return ViewHolder(view, onClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val book = booksList[position]
        holder.bind(book)
    }

    override fun getItemCount() = booksList.size
}
