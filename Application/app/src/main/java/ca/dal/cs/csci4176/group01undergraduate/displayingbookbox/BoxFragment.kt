package ca.dal.cs.csci4176.group01undergraduate.displayingbookbox

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ca.dal.cs.csci4176.group01undergraduate.databinding.FragmentBoxBinding
import androidx.recyclerview.widget.LinearLayoutManager
import ca.dal.cs.csci4176.group01undergraduate.addingbookbox.AddingBookBoxActivity
import com.google.firebase.database.FirebaseDatabase
import android.content.Intent
import ca.dal.cs.csci4176.group01undergraduate.addingbookbox.models.BookBoxLocation
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError




class BoxFragment : Fragment() {

    private var _binding: FragmentBoxBinding? = null
    private val binding get() = _binding!!

    private lateinit var bookBoxAdapter: BookBoxAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBoxBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        fetchBookBoxes()

        binding.addBoxButton.setOnClickListener {
            val intent = Intent(activity, AddingBookBoxActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupRecyclerView() {
        bookBoxAdapter = BookBoxAdapter(listOf(), this::onBookBoxClicked)
        binding.bookBoxRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = bookBoxAdapter
        }
    }

    private fun fetchBookBoxes() {
        // Fetch data from Firebase and update the adapter
        val databaseReference = FirebaseDatabase.getInstance().reference.child("bookBoxes")
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val bookBoxes = dataSnapshot.children.mapNotNull { child ->
                    // Manual parsing might be necessary if automatic mapping fails
                    val name = child.child("name").value as? String
                    val description = child.child("description").value as? String
                    val imageUrl = child.child("imageUrl").value as? String
                    val latitude = child.child("location/latitude").getValue(Double::class.java) ?: 0.0
                    val longitude = child.child("location/longitude").getValue(Double::class.java) ?: 0.0
                    val location = BookBoxLocation(latitude, longitude)

                    BookBox(name, location, description, imageUrl)
                }
                bookBoxAdapter.updateBookBoxes(bookBoxes)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error
            }
        })

    }

    private fun onBookBoxClicked(bookBox: BookBox) {
        // Handle the click event for each book box, e.g., navigate to a detail page, or show options to add, view, or delete
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
