package jp.ac.ncc.sugisakihuuta.myapplication.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.runBlocking

@Database(entities = [SavedShelter::class], version = 1, exportSchema = false)
abstract class SavedShelterDatabase : RoomDatabase() {

    abstract fun savedShelterDao(): SavedShelterDao

    companion object {
        @Volatile
        private var INSTANCE: SavedShelterDatabase? = null

        fun getDatabase(context: Context): SavedShelterDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SavedShelterDatabase::class.java,
                    "saved_shelter_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }

        // `suspend` 関数として定義
        suspend fun clearDatabase(context: Context) {
            val instance = getDatabase(context)
            instance.savedShelterDao().deleteAll()
        }

        // `runBlocking` を使って同期的に呼び出すためのメソッド
        fun clearDatabaseSync(context: Context) {
            runBlocking {
                clearDatabase(context)
            }
        }
    }
}

