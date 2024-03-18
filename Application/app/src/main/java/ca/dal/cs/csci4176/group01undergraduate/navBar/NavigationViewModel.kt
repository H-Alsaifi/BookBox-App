package ca.dal.cs.csci4176.group01undergraduate.navBar

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

// NavigationViewModel class managing navigation state based on intents
class NavigationViewModel(private val reducer: NavigationReducer) : ViewModel() {
    private val _state = MutableLiveData<NavigationState>()
    val state: LiveData<NavigationState> = _state

    init {
        // Initialize state with EXPLORE tab as the default selected tab
        _state.value = NavigationState(NavigationTab.EXPLORE)
    }

    // Process incoming intents to update state
    fun processIntents(intent: NavigationIntent) {
        val currentState = _state.value ?: return
        _state.value = reducer.reduce(currentState, intent)
    }
}