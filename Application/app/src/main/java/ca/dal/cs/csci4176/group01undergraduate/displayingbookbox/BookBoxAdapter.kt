package ca.dal.cs.csci4176.group01undergraduate.displayingbookbox

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ca.dal.cs.csci4176.group01undergraduate.databinding.ItemBookBoxBinding
import java.net.HttpURLConnection
import java.net.URL

class BookBoxAdapter(
    private var bookBoxes: List<BookBox>,
    private val onBookBoxClicked: (BookBox) -> Unit
) : RecyclerView.Adapter<BookBoxAdapter.BookBoxViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookBoxViewHolder {
        val binding = ItemBookBoxBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BookBoxViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookBoxViewHolder, position: Int) {
        val bookBox = bookBoxes[position]
        holder.bind(bookBox)
    }

    override fun getItemCount() = bookBoxes.size

    fun updateBookBoxes(newBookBoxes: List<BookBox>) {
        bookBoxes = newBookBoxes
        notifyDataSetChanged()
    }

    inner class BookBoxViewHolder(private val binding: ItemBookBoxBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(bookBox: BookBox) {
            binding.nameTextView.text = bookBox.name
            binding.locationTextView.text = bookBox.location
            binding.descriptionTextView.text = bookBox.description

            // Load the image on a background thread
            Thread {
                val bitmap = downloadImage(bookBox.imageUrl)
                binding.imageView.post {
                    binding.imageView.setImageBitmap(bitmap)
                }
            }.start()

            itemView.setOnClickListener {
                onBookBoxClicked(bookBox)
            }
        }

        private fun downloadImage(urlString: String?): Bitmap? {
            if (urlString == null) return null
            var bitmap: Bitmap? = null
            try {
                val url = URL(urlString)
                val connection = url.openConnection() as HttpURLConnection
                connection.doInput = true
                connection.connect()
                val inputStream = connection.inputStream
                bitmap = BitmapFactory.decodeStream(inputStream)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return bitmap
        }
    }
}
