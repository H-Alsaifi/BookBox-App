package ca.dal.cs.csci4176.group01undergraduate.view.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import ca.dal.cs.csci4176.group01undergraduate.view.fragments.MapsFragment
import ca.dal.cs.csci4176.group01undergraduate.view.fragments.MenuFragment
import ca.dal.cs.csci4176.group01undergraduate.view.fragments.ProfileFragment
import ca.dal.cs.csci4176.group01undergraduate.R
import ca.dal.cs.csci4176.group01undergraduate.view.fragments.SearchFragment
import ca.dal.cs.csci4176.group01undergraduate.intent.NavigationIntent
import ca.dal.cs.csci4176.group01undergraduate.navigation.NavigationReducer
import ca.dal.cs.csci4176.group01undergraduate.navigation.NavigationTab
import ca.dal.cs.csci4176.group01undergraduate.viewmodel.NavigationViewModel
import ca.dal.cs.csci4176.group01undergraduate.viewmodel.factory.NavigationViewModelFactory
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: NavigationViewModel
    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val reducer = NavigationReducer()
        val factory = NavigationViewModelFactory(reducer)

        viewModel = ViewModelProvider(this, factory)[NavigationViewModel::class.java]
        bottomNavigationView = findViewById(R.id.bottom_navigation)

        // Update the selected item in the bottom navigation based on the current state
        viewModel.state.value?.let { state ->
            val itemId = when (state.selectedTab) {
                NavigationTab.MENU -> R.id.nav_menu
                NavigationTab.EXPLORE -> R.id.nav_explore
                NavigationTab.SEARCH -> R.id.nav_search
                NavigationTab.ACCOUNT -> R.id.nav_account
            }
            bottomNavigationView.selectedItemId = itemId
        }

        // Set up the navigation bar item selection listener
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_explore -> viewModel.processIntents(NavigationIntent.GoToExplore)
                R.id.nav_search -> viewModel.processIntents(NavigationIntent.GoToSearch)
                R.id.nav_account -> viewModel.processIntents(NavigationIntent.GoToAccount)
                R.id.nav_menu -> viewModel.processIntents(NavigationIntent.GoToMenu)
            }
            true
        }

        // Observe the navigation state changes
        viewModel.state.observe(this) { state ->
            // Update the UI based on the current state
            when (state.selectedTab) {
                NavigationTab.EXPLORE -> showMapFragment() // change when Map fragment implemented
                NavigationTab.SEARCH -> showSearchFragment()  // change when search fragment implemented
                NavigationTab.ACCOUNT -> showProfileFragment() // change when account fragment implemented
                NavigationTab.MENU -> showMenuFragment()
            }

        }
    }

    private fun showMenuFragment() {
        val fragment = MenuFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun showProfileFragment() {
        val fragment = ProfileFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun showMapFragment(){
        val mapsFragment = MapsFragment()
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, mapsFragment)
            .commit()

    }
    private fun showSearchFragment() {
        val fragment = SearchFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()

    }
}