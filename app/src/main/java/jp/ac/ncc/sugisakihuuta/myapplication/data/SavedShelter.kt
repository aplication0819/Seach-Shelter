package jp.ac.ncc.sugisakihuuta.myapplication.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "saved_shelters")
data class SavedShelter(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val lat: Double,
    val lon: Double,
    val address: String,
    //val memo: String
) : Parcelable