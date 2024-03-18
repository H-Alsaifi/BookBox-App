import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.assignment2.R

// Define the Book data class
data class Book(val name: String, val author: String)

// BooksAdapter for the RecyclerView
class BooksAdapter(private val booksList: List<Book>) : RecyclerView.Adapter<BooksAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTextView: TextView = view.findViewById(R.id.bookNameTextView)
        val authorTextView: TextView = view.findViewById(R.id.bookAuthorTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.book_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val book = booksList[position]
        holder.nameTextView.text = book.name
        holder.authorTextView.text = "by ${book.author}"
    }

    override fun getItemCount() = booksList.size
}

// Main Activity for the search feature
class Search : AppCompatActivity() {

    private lateinit var booksAdapter: BooksAdapter
    private val allBooks = listOf(
        Book("Kotlin for Beginners", "John Doe"),
        Book("Advanced Kotlin", "Jane Smith"),
        Book("Android Development", "Anne Brown"),
        Book("Java Fundamentals", "Gary White")
    )
    private val booksList = mutableListOf<Book>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_books)

        val searchQueryEditText: EditText = findViewById(R.id.searchQueryEditText)
        val searchByNameCheckbox: CheckBox = findViewById(R.id.searchByNameCheckbox)
        val searchByAuthorCheckbox: CheckBox = findViewById(R.id.searchByAuthorCheckbox)
        val searchButton: Button = findViewById(R.id.searchButton)
        val booksRecyclerView: RecyclerView = findViewById(R.id.booksRecyclerView)

        booksRecyclerView.layoutManager = LinearLayoutManager(this)
        booksAdapter = BooksAdapter(booksList)
        booksRecyclerView.adapter = booksAdapter

        searchButton.setOnClickListener {
            val queryText = searchQueryEditText.text.toString()
            val searchByName = searchByNameCheckbox.isChecked
            val searchByAuthor = searchByAuthorCheckbox.isChecked
            searchBooks(queryText, searchByName, searchByAuthor)
        }
    }

    private fun searchBooks(queryText: String, searchByName: Boolean, searchByAuthor: Boolean) {
        booksList.clear()
        val filteredBooks = allBooks.filter { book ->
            (searchByName && book.name.contains(queryText, ignoreCase = true)) ||
                    (searchByAuthor && book.author.contains(queryText, ignoreCase = true))
        }
        booksList.addAll(filteredBooks)
        booksAdapter.notifyDataSetChanged()
    }
}
