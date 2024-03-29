package ca.dal.cs.csci4176.group01undergraduate.navigation

import ca.dal.cs.csci4176.group01undergraduate.intent.NavigationIntent
import ca.dal.cs.csci4176.group01undergraduate.state.NavigationState

// Class to handle state reduction based on received intents
class NavigationReducer {
    // Reduce function takes current state and intent, returns new state
    fun reduce(currentState: NavigationState, intent: NavigationIntent): NavigationState {
        return when (intent) {
            is NavigationIntent.GoToExplore -> currentState.copy(selectedTab = NavigationTab.EXPLORE)
            is NavigationIntent.GoToSearch -> currentState.copy(selectedTab = NavigationTab.SEARCH)
            is NavigationIntent.GoToAccount -> currentState.copy(selectedTab = NavigationTab.ACCOUNT)
            is NavigationIntent.GoToMenu -> currentState.copy(selectedTab = NavigationTab.MENU)
        }
    }
}
