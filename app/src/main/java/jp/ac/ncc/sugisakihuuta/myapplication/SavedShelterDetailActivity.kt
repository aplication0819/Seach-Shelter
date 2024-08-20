package jp.ac.ncc.sugisakihuuta.myapplication

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SavedShelterDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.saved_shelter_list_item) // 使用するレイアウトファイルを設定

        // Intentからデータを取得
        val shelterName = intent.getStringExtra("shelter_name")
        val shelterLat = intent.getDoubleExtra("shelter_lat", 0.0)
        val shelterLon = intent.getDoubleExtra("shelter_lon", 0.0)

        // データを表示する
        val shelterNameTextView = findViewById<TextView>(R.id.shelter_name_text_view)
        shelterNameTextView.text = shelterName


    }
}
