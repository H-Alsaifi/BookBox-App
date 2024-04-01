package ca.dal.cs.csci4176.group01undergraduate.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ca.dal.cs.csci4176.group01undergraduate.databinding.FragmentBoxBinding
import androidx.recyclerview.widget.LinearLayoutManager
import ca.dal.cs.csci4176.group01undergraduate.view.activities.AddingBookBoxActivity
import com.google.firebase.database.FirebaseDatabase
import android.content.Intent
import android.util.Log
import ca.dal.cs.csci4176.group01undergraduate.model.BookBoxLocation
import ca.dal.cs.csci4176.group01undergraduate.model.BookBox
import ca.dal.cs.csci4176.group01undergraduate.view.adapters.BookBoxAdapter
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError




class BoxFragment : Fragment() {

    // Binding property to access the layout's views.
    private var _binding: FragmentBoxBinding? = null
    // A non-nullable version of the binding property for easy access. It throws an exception if _binding is null.
    private val binding get() = _binding!!

    // Adapter for the RecyclerView that displays book boxes.
    private lateinit var bookBoxAdapter: BookBoxAdapter

    // Inflates the layout for this fragment.
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBoxBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Called immediately after onCreateView() has returned, but before any saved state has been restored into the view.
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView() // Initializes and sets up the RecyclerView.
        fetchBookBoxes() // Fetches the list of book boxes from the database.

        // Sets up a click listener for the 'add box' button.
        binding.addBoxButton.setOnClickListener {
            val intent = Intent(activity, AddingBookBoxActivity::class.java)
            startActivity(intent) // Starts the activity to add a new book box.
        }
    }

    // Initializes the RecyclerView and its adapter.
    private fun setupRecyclerView() {
        bookBoxAdapter = BookBoxAdapter(listOf(), requireContext(), this::onBookBoxClicked)
        binding.bookBoxRecyclerView.apply {
            layoutManager = LinearLayoutManager(context) // Sets the layout manager.
            adapter = bookBoxAdapter // Sets the adapter for the RecyclerView.
        }
    }

    // Fetches the list of book boxes from the Firebase database.
    private fun fetchBookBoxes() {
        val databaseReference = FirebaseDatabase.getInstance().reference.child("bookBoxes")
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Maps each dataSnapshot child to a BookBox object.
                val bookBoxes = dataSnapshot.children.mapNotNull { child ->
                    val description = child.child("description").value as? String
                    val imageUrl = child.child("imageUrl").value as? String
                    val latitude = child.child("latitude").getValue(Double::class.java)
                    val longitude = child.child("longitude").getValue(Double::class.java)
                    val address = child.child("address").value as? String

                    if (latitude != null && longitude != null) {
                        val location = BookBoxLocation(latitude, longitude)
                        val bookIDs = child.child("bookIDs").children.map { it.key ?: "" }.toMutableList()

                        BookBox(location, address, description, imageUrl, bookIDs) // Constructs a BookBox object.
                    } else {
                        null // Returns null if latitude or longitude is missing, filtering out incomplete entries.
                    }
                }
                bookBoxAdapter.updateBookBoxes(bookBoxes) // Updates the adapter with the new list of book boxes.
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("BoxFragment", "Failed to fetch data", databaseError.toException()) // Logs an error if data fetch is cancelled.
            }
        })
    }

    // Defines what happens when a book box in the list is clicked.
    private fun onBookBoxClicked(bookBox: BookBox) {

    }

    // Cleans up the binding when the view is destroyed to prevent memory leaks.
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
