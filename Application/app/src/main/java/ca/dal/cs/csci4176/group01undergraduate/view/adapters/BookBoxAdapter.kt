package ca.dal.cs.csci4176.group01undergraduate.view.adapters

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ca.dal.cs.csci4176.group01undergraduate.coords
import ca.dal.cs.csci4176.group01undergraduate.databinding.ItemBookBoxBinding
import java.net.HttpURLConnection
import java.net.URL
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import ca.dal.cs.csci4176.group01undergraduate.model.BookBox
import ca.dal.cs.csci4176.group01undergraduate.model.BookBoxLocation

// Adapter class for managing the display of book boxes in a RecyclerView.
// This class is responsible for converting each book box data item into view items within the RecyclerView.
class BookBoxAdapter(
    private var bookBoxes: List<BookBox>,
    private val context: Context,
    private val onBookBoxClicked: (BookBox) -> Unit
) : RecyclerView.Adapter<BookBoxAdapter.BookBoxViewHolder>() {

    // Creates new views for book boxes.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookBoxViewHolder {
        // Inflate the view for each individual item in the list.
        val binding = ItemBookBoxBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BookBoxViewHolder(binding)
    }

    // Replaces the contents of a view (invoked by the layout manager).
    override fun onBindViewHolder(holder: BookBoxViewHolder, position: Int) {
        // Get the book box at the given position in the list and bind it to the holder.
        val bookBox = bookBoxes[position]
        holder.bind(bookBox)
    }

    // Returns the size of the dataset (invoked by the layout manager).
    override fun getItemCount() = bookBoxes.size

    // Updates the list of book boxes and notifies the adapter to refresh the view.
    fun updateBookBoxes(newBookBoxes: List<BookBox>) {
        bookBoxes = newBookBoxes
        notifyDataSetChanged()
    }

    // ViewHolder class for book boxes. Holds the view for each book box item.
    inner class BookBoxViewHolder(private val binding: ItemBookBoxBinding) : RecyclerView.ViewHolder(binding.root) {
        // Binds a book box item to the view.
        fun bind(bookBox: BookBox) {
            // Attempt to get a human-readable address for the book box location.
            val location = bookBox.location

            binding.locationTextView.text = bookBox.address

            // Set the description text.
            binding.descriptionTextView.text = bookBox.description
            // Load and set the book box image asynchronously.
            Thread {
                val bitmap = downloadImage(bookBox.imageUrl)
                binding.imageView.post {
                    binding.imageView.setImageBitmap(bitmap)
                }
            }.start()

            // adding the selected bookbox to the users favorite list
            binding.favBoxBtn.setOnClickListener {
                // getting the id of the currently logged in user to save the favorite in their account
                val userId = FirebaseAuth.getInstance().currentUser?.uid.toString()
                val loc = bookBox.location
                val userRef = FirebaseDatabase.getInstance().getReference("users").child(userId)
                // saving the coords of the favorite box in the users favorites
                val coord: coords? =
                    location?.let { it1 -> bookBox.address?.let { it2 ->
                        coords(location.latitude, it1.longitude,
                            it2
                        )
                    } }
//                val coords: coords? = loc?.let { it1 -> location?.let { it2 ->
//                    coords(it1.longitude,
//                        it2.latitude)
//                } }
                // saving the new favorite book box coordinates
                userRef.child("favorites").push().setValue(coord)
            }

            // Set a click listener to handle user interaction with the book box item.
            itemView.setOnClickListener {
                onBookBoxClicked(bookBox)
            }
        }

        // Downloads an image from a URL and returns it as a Bitmap.
        private fun downloadImage(urlString: String?): Bitmap? {
            if (urlString == null) return null // Return null if the URL is not provided.
            var bitmap: Bitmap? = null
            try {
                // Open a connection to the URL and decode the stream into a Bitmap.
                val url = URL(urlString)
                val connection = url.openConnection() as HttpURLConnection
                connection.doInput = true
                connection.connect()
                val inputStream = connection.inputStream
                bitmap = BitmapFactory.decodeStream(inputStream)
            } catch (e: Exception) {
                e.printStackTrace() // Log any exceptions during the image download process.
            }
            return bitmap
        }
    }
}
