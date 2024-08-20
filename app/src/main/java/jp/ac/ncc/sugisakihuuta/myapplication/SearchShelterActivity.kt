package jp.ac.ncc.sugisakihuuta.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.SearchView
import android.widget.Toast
import android.app.Activity
import androidx.appcompat.app.AppCompatActivity

class SearchShelterActivity : AppCompatActivity() {

    private lateinit var searchView: SearchView
    private lateinit var searchResultsListView: ListView
    private lateinit var shelters: List<Shelter>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_shelter)

        searchView = findViewById(R.id.search)
        searchResultsListView = findViewById(R.id.searchResultsListView)

        val searchView = findViewById<SearchView>(R.id.search)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    searchShelter(it)
                }

                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let {
                    searchShelter(it)
                }
                return true
            }
        })

        searchResultsListView.setOnItemClickListener { parent, view, position, id ->
            // タップされた項目の避難場所を取得
            val selectedShelter = shelters[position]

            // MainActivityに選択された避難場所の情報を渡すIntentを作成
            val intent = Intent()
            intent.putExtra("selectedShelter", selectedShelter)
            setResult(Activity.RESULT_OK, intent)
            finish() // Activityを終了してMainActivityに戻る
        }
    }

    private fun searchShelter(query: String) {
        shelters = ShelterHelper.searchShelterByName(query)

        val adapter = ShelterAdapter(this,shelters)
        searchResultsListView.adapter = adapter
    }
}