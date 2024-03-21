package ca.dal.cs.csci4176.group01undergraduate.addingbookbox.reducers

import ca.dal.cs.csci4176.group01undergraduate.addingbookbox.intents.AddBookBoxIntent
import ca.dal.cs.csci4176.group01undergraduate.addingbookbox.states.AddBookBoxState

class AddBookBoxReducer {
    fun reduce(currentState: AddBookBoxState, intent: AddBookBoxIntent): AddBookBoxState {
        return when (intent) {
            is AddBookBoxIntent.OpenAddBookBox -> currentState // Handle open add box view
            is AddBookBoxIntent.SubmitDetails -> currentState.copy(/* new state values */)
            is AddBookBoxIntent.UploadPicture -> currentState.copy(/* new state values */)
            is AddBookBoxIntent.ConfirmAddition -> currentState.copy(/* new state values */)
            // Add additional cases for other intents
        }
    }
}
