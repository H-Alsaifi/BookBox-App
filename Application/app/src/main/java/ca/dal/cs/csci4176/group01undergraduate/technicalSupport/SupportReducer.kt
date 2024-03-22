package ca.dal.cs.csci4176.group01undergraduate.technicalSupport

class SupportReducer {
    fun reduce(currentState: SupportState, intent: SupportIntent): SupportState {
        return when (intent) {
            is SupportIntent.ShowContactForm -> SupportState.ShowContactFormState
            // Handle other intents and return new states
        }
    }
}
