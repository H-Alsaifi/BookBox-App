package ca.dal.cs.csci4176.group01undergraduate.navBar

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
