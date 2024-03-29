package ca.dal.cs.csci4176.group01undergraduate.view.fragments

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import android.widget.Button
import ca.dal.cs.csci4176.group01undergraduate.R

@Suppress("DEPRECATION")
class MenuFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_menu, container, false)

        val themeSwitch = view.findViewById<SwitchCompat>(R.id.switchDarkMode)
        themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }

        // Restore the state of the switch based on the current theme mode
        val nightModeFlags = context?.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)
        themeSwitch.isChecked = nightModeFlags == Configuration.UI_MODE_NIGHT_YES

        // Find the button and set a click listener
        val buttonContactSupport = view.findViewById<Button>(R.id.buttonContactSupport)
        buttonContactSupport.setOnClickListener {
            // Replace the current fragment with ContactFormFragment
            fragmentManager?.beginTransaction()?.replace(R.id.fragment_container, ContactFormFragment())?.addToBackStack(null)?.commit()
        }

        val buttonAboutUs= view.findViewById<Button>(R.id.buttonAboutUs)
        buttonAboutUs.setOnClickListener {
            fragmentManager?.beginTransaction()?.replace(R.id.fragment_container, AboutUsFragment())?.addToBackStack(null)?.commit()
        }

        return view
    }
}
