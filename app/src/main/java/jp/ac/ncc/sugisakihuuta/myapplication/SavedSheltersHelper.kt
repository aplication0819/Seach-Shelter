package jp.ac.ncc.sugisakihuuta.myapplication

import android.content.Context
import android.content.Intent
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import jp.ac.ncc.sugisakihuuta.myapplication.data.SavedShelter
import jp.ac.ncc.sugisakihuuta.myapplication.data.SavedShelterDao
import jp.ac.ncc.sugisakihuuta.myapplication.data.SavedShelterDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SavedSheltersHelper(private val lifecycleOwner: LifecycleOwner, private val context: Context) {

    private lateinit var savedSheltersListView: RecyclerView
    private lateinit var savedSheltersAdapter: SavedSheltersAdapter
    private lateinit var savedShelterDao: SavedShelterDao

    init {
        // SavedShelterDaoの初期化
        savedShelterDao = SavedShelterDatabase.getDatabase(context).savedShelterDao()
    }

    fun setupSavedSheltersListView(savedSheltersListView: RecyclerView) {
        this.savedSheltersListView = savedSheltersListView

        // onShelterClickの処理を定義
        val onShelterClick: (SavedShelter) -> Unit = { shelter ->
            showLoadingScreen()
            pickUpShelter(shelter)
        }

        // onDeleteShelterの処理を定義
        val onDeleteShelter: (SavedShelter) -> Unit = { shelter ->
            deleteShelter(shelter)
        }

        // Adapterの初期化
        savedSheltersAdapter = SavedSheltersAdapter(context, onShelterClick, onDeleteShelter)
        savedSheltersListView.adapter = savedSheltersAdapter
        savedSheltersListView.layoutManager = LinearLayoutManager(context)

        // LiveDataを観察してデータの変更に対応
        observeSavedShelters()
    }

    private fun observeSavedShelters() {
        // データベースのLiveDataを観察
        savedShelterDao.getAllSavedShelters().observe(lifecycleOwner, Observer { savedShelters ->
            // RecyclerViewのデータを更新
            savedSheltersAdapter.updateData(savedShelters)
        })
    }

    private fun showLoadingScreen() {
        val loadingIntent = Intent(context, LoadingActivity::class.java)
        context.startActivity(loadingIntent)
    }

    private fun pickUpShelter(shelter: SavedShelter) {
        lifecycleOwner.lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                // 必要な処理がある場合、ここで行う

                // 遅延を加える
                delay(2000) // 2秒待つ

                // メインスレッドでUI操作を行う
                withContext(Dispatchers.Main) {
                    val intent = Intent(context, SavedShelterDetailActivity::class.java).apply {
                        putExtra("shelter_name", shelter.name)
                        putExtra("shelter_lat", shelter.lat) // 緯度を渡す
                        putExtra("shelter_lon", shelter.lon) // 経度を渡す
                    }
                    context.startActivity(intent)
                }
            }

            // メインスレッドに戻って処理を実行
            withContext(Dispatchers.Main) {
                val mainIntent = Intent(context, MainActivity::class.java).apply {
                    putExtra("shelter_name", shelter.name)
                    putExtra("shelter_lat", shelter.lat) // 緯度を渡す
                    putExtra("shelter_lon", shelter.lon) // 経度を渡す
                }
                context.startActivity(mainIntent)

                // LoadingActivityを終了
                finishLoadingActivity()
            }
        }
    }

    private fun finishLoadingActivity() {
        // LoadingActivityを終了するためのコード
        // 現在のアクティビティを終了する
        if (context is LoadingActivity) {
            context.finish()
        }
    }

    fun saveShelter(shelter: SavedShelter) {
        lifecycleOwner.lifecycleScope.launch {
            savedShelterDao.insert(shelter)
        }
    }

    private fun deleteShelter(shelter: SavedShelter) {
        lifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            savedShelterDao.delete(shelter)

            // すべての避難場所を取得
            val allShelters = savedShelterDao.getAllSavedSheltersList()

            // IDを1から再割り当て
            allShelters.forEachIndexed { index, savedShelter ->
                savedShelterDao.updateId(savedShelter.id, index + 1)
            }


        }
    }

}
