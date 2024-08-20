package jp.ac.ncc.sugisakihuuta.myapplication.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Delete

@Dao
interface SavedShelterDao {
    @Insert//(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(savedShelter: SavedShelter)

    @Query("SELECT * FROM saved_shelters /*ORDER BY name ASC*/")
    fun getAllSavedShelters(): LiveData<List<SavedShelter>>

    @Query("SELECT * FROM saved_shelters ORDER BY id ASC")
    suspend fun getAllSavedSheltersList(): List<SavedShelter>


    @Delete
    suspend fun delete(savedShelter: SavedShelter)

    @Query("DELETE FROM saved_shelters")
    suspend fun deleteAll()

    @Query("SELECT MAX(id) FROM saved_shelters")
    suspend fun getMaxId(): Int?

    // 追加するメソッド
   /* @Query("SELECT * FROM saved_shelters")
    suspend fun getAllSavedSheltersList(): List<SavedShelter>

    @Query("UPDATE saved_shelters SET id = :newId WHERE id = :oldId")
    suspend fun updateId(oldId: Int, newId: Int)*/

    @Query("UPDATE saved_shelters SET id = :newId WHERE id = :oldId")
    suspend fun updateId(oldId: Int, newId: Int)

}
