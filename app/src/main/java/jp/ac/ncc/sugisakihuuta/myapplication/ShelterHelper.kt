package jp.ac.ncc.sugisakihuuta.myapplication

import android.location.Location
import android.util.Log
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader

object ShelterHelper {

    private const val TAG = "ShelterHelper"

    private val shelters: MutableList<Shelter> = mutableListOf()

    /**
     * CSVファイルから避難所のデータを読み込みます。
     *
     * @param inputStream CSVファイルの入力ストリーム
     */
    fun loadSheltersFromCSV(inputStream: InputStream) {
        shelters.clear()
        val reader = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8))

        try {
            var line: String? = reader.readLine() // 最初の行（ヘッダー行）を読み込む
            var lineNumber = 1

            // UTF-8 BOM の処理
            if (line != null && line.startsWith("\uFEFF")) {
                line = line.substring(1) // BOM を除去
            }

            while (line != null) {
                lineNumber++
                val parts = line.split(",")
                if (parts.size >= 16) {
                    try {
                        // 緯度と経度のチェック（空でないこと）
                        val latitudeString = parts[14].trim()
                        val longitudeString = parts[15].trim()
                        if (latitudeString.isNotEmpty() && longitudeString.isNotEmpty()) {
                            // 避難場所として指定されているかをチェック
                            val isShelter =
                                parts[5].isNotEmpty() || parts[6].isNotEmpty() || parts[7].isNotEmpty() ||
                                        parts[8].isNotEmpty() || parts[9].isNotEmpty() || parts[10].isNotEmpty() ||
                                        parts[11].isNotEmpty() || parts[12].isNotEmpty()

                            if (isShelter) {
                                val name = parts[3] // 施設名
                                val latitude = latitudeString.toDouble() // 緯度
                                val longitude = longitudeString.toDouble() // 経度
                                val address = parts[4].trim() // 住所
                                val shelter = Shelter(name, latitude, longitude, address)
                                shelters.add(shelter)
                            }
                        }
                    } catch (e: NumberFormatException) {
                        Log.e(TAG, "Error parsing CSV line $lineNumber: $line", e)
                    }
                }
                line = reader.readLine()
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error reading CSV", e)
        } finally {
            try {
                reader.close()
            } catch (e: IOException) {
                Log.e(TAG, "Error closing reader", e)
            }
        }
    }

    /**
     * 現在の位置から最も近い避難所を取得します。
     *
     * @param currentLocation 現在の位置
     * @return 最も近い避難所
     */
    fun getClosestShelter(currentLocation: Location): Shelter? {
        var closestShelter: Shelter? = null
        var minDistance = Float.MAX_VALUE
        for (shelter in shelters) {
            val distance = FloatArray(1)
            Location.distanceBetween(
                currentLocation.latitude, currentLocation.longitude,
                shelter.latitude, shelter.longitude,
                distance
            )
            if (distance[0] < minDistance) {
                minDistance = distance[0]
                closestShelter = shelter
            }
        }
        return closestShelter
    }

    /**
     * 現在の位置から指定された半径内の避難所を取得します。
     *
     * @param currentLocation 現在の位置
     * @param radius 検索半径（メートル単位）
     * @return 指定された半径内の避難所のリスト
     */
    fun getNearbyShelters(currentLocation: Location, radius: Float): List<Shelter> {
        val nearbyShelters = mutableListOf<Shelter>()
        for (shelter in shelters) {
            val distance = FloatArray(1)
            Location.distanceBetween(
                currentLocation.latitude, currentLocation.longitude,
                shelter.latitude, shelter.longitude,
                distance
            )
            if (distance[0] <= radius) {
                nearbyShelters.add(shelter)
            }
        }
        return nearbyShelters
    }

    /**
     * 指定された名前を含む避難所を検索し、リストで返します。
     *
     * @param name 検索する避難所の名前（部分一致）
     * @return 名前に一致する避難所のリスト
     */
    fun searchShelterByName(name: String): List<Shelter> {
        val result = mutableListOf<Shelter>()
        for (shelter in shelters) {
            if (shelter.name.contains(name, ignoreCase = true)) {
                result.add(shelter)
            }
        }
        return result
    }
    // クエリで避難場所を検索するメソッド
    fun searchShelters(query: String): List<Shelter> {
        // ここでは、名前によるフィルタリングを実行しています
        return shelters.filter { it.name.contains(query, ignoreCase = true) }
    }
}
