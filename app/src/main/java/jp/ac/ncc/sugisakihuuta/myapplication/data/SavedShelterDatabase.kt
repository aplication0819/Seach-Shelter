package jp.ac.ncc.sugisakihuuta.myapplication.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

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
    }

}

