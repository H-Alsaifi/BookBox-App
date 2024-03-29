package ca.dal.cs.csci4176.group01undergraduate.intent

// Define sealed class for navigation intents to encapsulate different navigation actions
sealed class NavigationIntent {
    object GoToExplore : NavigationIntent()
    object GoToSearch : NavigationIntent()
    object GoToAccount : NavigationIntent()
    object GoToMenu : NavigationIntent()
}
