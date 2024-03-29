package ca.dal.cs.csci4176.group01undergraduate.view.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ca.dal.cs.csci4176.group01undergraduate.databinding.ItemBookBoxBinding
import java.net.HttpURLConnection
import java.net.URL
import android.location.Geocoder
import java.io.IOException
import android.util.Log
import ca.dal.cs.csci4176.group01undergraduate.model.BookBox

// Adapter class for managing the display of book boxes in a RecyclerView.
// This class is responsible for converting each book box data item into view items within the RecyclerView.
class BookBoxAdapter(
    private var bookBoxes: List<BookBox>,
    private val context: Context,
    private val onBookBoxClicked: (BookBox) -> Unit
) : RecyclerView.Adapter<BookBoxAdapter.BookBoxViewHolder>() {
    // Geocoder instance for converting geographic locations to human-readable addresses.
    private val geocoder = Geocoder(context)

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
        @SuppressLint("SetTextI18n")
        fun bind(bookBox: BookBox) {
            // Attempt to get a human-readable address for the book box location.
            val location = bookBox.location
            if (location != null) {
                Log.d("BookBoxAdapter", "Coordinates before Geocoder: Lat: ${location.latitude}, Lon: ${location.longitude}")
                try {
                    // Try to get the address using Geocoder.
                    val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                    val addressText = addresses?.let { addressList ->
                        if (addressList.isNotEmpty()) addressList[0].getAddressLine(0) // Get the full address if available
                        else "Lat: ${location.latitude}, Lon: ${location.longitude}" // Fallback to coordinates if address not found
                    } ?: "No Address Found"
                    binding.locationTextView.text = addressText
                } catch (e: IOException) {
                    Log.e("BookBoxAdapter", "Geocoder IOException", e)
                    // Fallback to coordinates if there's an error with Geocoder.
                    binding.locationTextView.text = "Lat: ${location.latitude}, Lon: ${location.longitude}"
                    e.printStackTrace()
                }
            } else {
                Log.d("BookBoxAdapter", "Location is null")
                // Display a default message if the location is not available.
                binding.locationTextView.text = "Location not available"
            }

            // Set the description text.
            binding.descriptionTextView.text = bookBox.description
            // Load and set the book box image asynchronously.
            Thread {
                val bitmap = downloadImage(bookBox.imageUrl)
                binding.imageView.post {
                    binding.imageView.setImageBitmap(bitmap)
                }
            }.start()

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
