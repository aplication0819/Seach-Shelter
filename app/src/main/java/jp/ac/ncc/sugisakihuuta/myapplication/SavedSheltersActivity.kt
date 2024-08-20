package jp.ac.ncc.sugisakihuuta.myapplication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView

class SavedSheltersActivity : AppCompatActivity() {

    private lateinit var savedSheltersHelper: SavedSheltersHelper
    private lateinit var savedSheltersListView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_saved_shelters)

        savedSheltersListView = findViewById(R.id.saved_shelters_recycler_view)
        savedSheltersHelper = SavedSheltersHelper(this, this)
        savedSheltersHelper.setupSavedSheltersListView(savedSheltersListView)
    }
}
