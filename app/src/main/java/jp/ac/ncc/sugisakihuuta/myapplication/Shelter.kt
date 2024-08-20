package jp.ac.ncc.sugisakihuuta.myapplication

import com.google.android.gms.maps.model.LatLng
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Shelter(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val address: String
    //val lat: Double,  // 緯度
    //val lng: Double   // 経度
) : Parcelable {
    // location プロパティはそのまま保持
    val location: LatLng
        get() = LatLng(latitude, longitude)
}
