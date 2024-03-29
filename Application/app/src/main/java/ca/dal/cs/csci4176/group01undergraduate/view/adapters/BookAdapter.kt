package ca.dal.cs.csci4176.group01undergraduate.view.adapters
//importing the needed libraries
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ca.dal.cs.csci4176.group01undergraduate.R
import ca.dal.cs.csci4176.group01undergraduate.model.Book
/**
 * an adapter dor a RecyclerView to display lists of books
 * @param booksList the list of the books
 * @param onClick a callback function that will be invoked when a user clicks on a book
 */
class BooksAdapter(private val booksList: List<Book>, private val onClick: (Book) -> Unit) : RecyclerView.Adapter<BooksAdapter.ViewHolder>() {

    /**
     * a ViewHolder to told the refernce of the views for the items in RecyclerView
     * @param viewing the items
     * @param onClick the invoked callback functions
     */
    class ViewHolder(view: View, val onClick: (Book) -> Unit) : RecyclerView.ViewHolder(view) {
        //initialing variables to display information about the book
        val addressTextView: TextView = view.findViewById(R.id.bookBoxAddressTextView)
        val titleTextView: TextView = view.findViewById(R.id.bookTitleTextView)
        val authorTextView: TextView = view.findViewById(R.id.bookAuthorTextView)
        private var currentBook: Book? = null

        //setting up a click listener for the current book to be opened when pressed
        init {
            view.setOnClickListener {
                currentBook?.let {
                    onClick(it)
                }
            }
        }

        /**
         * binding the data of the book with the views
         * @param book an object to be binded
         */
        fun bind(book: Book) {
            //updating the information of the book
            currentBook = book
            titleTextView.text = "Title: ${book.title}"
            authorTextView.text = "Author: ${book.author}"
            addressTextView.text = "Location: ${book.address}"
        }
    }

    /**
     * an oncreate to be called when the RecyclerView needs a ViewHolder to show item
     * @param parent the new view would be added here
     * @param viewType the type of the new view
     * @return  a new viewholder with the view of the new view type
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        //initializing a new variable to inflate the item layout
        val view = LayoutInflater.from(parent.context).inflate(R.layout.book_item, parent, false)
        //returning the view
        return ViewHolder(view, onClick)
    }

    /**
     * an onbind to display the data
     * @param holder a holder to show the information
     * @param position to show the position of the information shown
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val book = booksList[position]
        holder.bind(book)
    }

    /**
     * a class to return the total amount of the books
     * @return the number of the books
     */
    override fun getItemCount() = booksList.size
}
