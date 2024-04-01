package ca.dal.cs.csci4176.group01undergraduate.viewmodel.factory
//importing the needed libraries
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ca.dal.cs.csci4176.group01undergraduate.repository.BookRepository
import ca.dal.cs.csci4176.group01undergraduate.viewmodel.BookViewModel

//creating instance of a view model with the bookrepository
class BookViewModelFactory(private val repository: BookRepository) : ViewModelProvider.Factory {
    //a function called whenever new viewmodel is called and created
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        //check if bookviewmodel is a viewmodel
        if (modelClass.isAssignableFrom(BookViewModel::class.java)) {
            //if it is then create a new instance for the bookviewmodel
            @Suppress("UNCHECKED_CAST")
            return BookViewModel(repository) as T
        }
        //throwing an exception if the view model is not available
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}