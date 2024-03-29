package ca.dal.cs.csci4176.group01undergraduate.view.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.LinearLayout
import ca.dal.cs.csci4176.group01undergraduate.R
import ca.dal.cs.csci4176.group01undergraduate.view.fragments.MapsFragment

import com.google.android.material.bottomsheet.BottomSheetBehavior

class InteractiveMap : AppCompatActivity() {

    lateinit var bottomSheetBehavior : BottomSheetBehavior<LinearLayout>
    private lateinit var bottomSheetLayout: LinearLayout

    /**
     * Creates the activity
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // set content view
        setContentView(R.layout.activity_interactive_map)

        // bottom sheet
        bottomSheetLayout = findViewById(R.id.bottomSheetLayout)
        bottomSheetBehavior = BottomSheetBehavior
            .from(bottomSheetLayout)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

        MapsFragment()
    }
}