package jp.ac.ncc.sugisakihuuta.myapplication

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.view.MotionEvent
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import android.widget.FrameLayout
import android.widget.RelativeLayout
import android.widget.ListView
import android.app.Activity
import android.widget.ListAdapter
import android.content.Context
import android.widget.BaseAdapter
import android.view.ViewGroup
import android.view.LayoutInflater
import android.widget.TextView

import android.graphics.Point
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.RecyclerView

import android.location.Geocoder
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.material.navigation.NavigationView
import java.util.Locale

import java.io.IOException
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import jp.ac.ncc.sugisakihuuta.myapplication.data.SavedShelter
import jp.ac.ncc.sugisakihuuta.myapplication.data.SavedShelterDatabase
import jp.ac.ncc.sugisakihuuta.myapplication.data.SavedShelterDao
import android.view.GestureDetector
import com.google.android.gms.maps.model.BitmapDescriptorFactory


class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    companion object {
        private const val TAG = "MainActivity"
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
        private const val REQUEST_CODE_LOCATION_SETTING = 2001
        const val REQUEST_CODE_SEARCH_SHELTER = 1001

        //const val REQUEST_CODE_SAVED_SHELTER_DETAIL = 1001 // 任意の数値を指定できます

        private val JAPAN_BOUNDS = LatLngBounds(
            LatLng(24.396308, 122.93457),
            LatLng(45.551483, 153.986672)
        )
    }

    private lateinit var mMap: GoogleMap
    //private val markerList = mutableListOf<Marker>()
    private lateinit var locationSensor: LocationSensor
    private lateinit var findShelterButton: Button
    private lateinit var distanceSpinner: Spinner

    private var userMovedMap = false
    private var mapView: MapView? = null
    private var lastKnownLocation: Location? = null
    private var initialZoomDone = false  // 初回ズーム完了のフラグ
    private var selectedDistance = 0f
    private var currentCircle: Circle? = null
    private lateinit var gestureDetector: GestureDetector
    //private var shelterMarkers: MutableList<Marker> = mutableListOf()
    private val shelterMarkers = mutableListOf<Marker>()

    // Drawer-related properties
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var navigationView: NavigationView
    private lateinit var toolbar: Toolbar

    private lateinit var savedShelterDao: SavedShelterDao
    private lateinit var savedSheltersAdapter: SavedSheltersAdapter

    private lateinit var savedSheltersHelper: SavedSheltersHelper
    private lateinit var savedSheltersListView: RecyclerView

    private lateinit var loadingScreen: FrameLayout

    // クラスレベルでフラグを宣言
    private var isInitialZoomCompleted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // ロード画面を表示
        loadingScreen = findViewById(R.id.loading_screen) as FrameLayout
        showLoadingScreen()

        val nowLoadingTextView: TextView = findViewById(R.id.tvNowLoading)
        val animation = AnimationUtils.loadAnimation(this, R.anim.nowloading_animation)
        nowLoadingTextView.startAnimation(animation)


        savedSheltersHelper = SavedSheltersHelper(this,this)

        // Adapterの初期化
       /* savedSheltersAdapter = SavedSheltersAdapter(
            context = this,
            onDeleteShelter = { shelter -> deleteShelter(shelter) }
        )*/

        // データベースとDaoの初期化
        val db = SavedShelterDatabase.getDatabase(applicationContext)
        savedShelterDao = db.savedShelterDao()

        //savedSheltersListView = findViewById(R.id.saved_shelters_list_view)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, getString(R.string.google_maps_key))
        }

        locationSensor = LocationSensor(this)

        findShelterButton = findViewById(R.id.find_shelter_button)
        distanceSpinner = findViewById(R.id.distance_spinner)


        val inputStream = resources.openRawResource(R.raw.h27_todofuken_tatemono)
        val shelters = ShelterHelper.loadSheltersFromCSV(inputStream)

        val distanceOptions = listOf("最も近い", "1km", "2km", "3km", "4km", "5km")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, distanceOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        distanceSpinner.adapter = adapter

        distanceSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedDistance = when (position) {
                    0 -> 0f // 最も近い
                    else -> (position) * 1000f
                }
                drawCircle(selectedDistance)
                adjustZoomLevelForRadius(selectedDistance)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }

        findShelterButton.setOnClickListener {
            clearAllMarkers()
            requestLocationSetting() // 位置情報の設定を確認

            val currentLocation = locationSensor.location.value
            if (currentLocation != null) {
                if (selectedDistance == 0f) {
                    val closestShelter = ShelterHelper.getClosestShelter(currentLocation)
                    if (closestShelter != null) {
                        updateSheltersUI(listOf(closestShelter))
                    }
                } else {
                    val nearbyShelters =
                        ShelterHelper.getNearbyShelters(currentLocation, selectedDistance)
                    updateSheltersUI(nearbyShelters)
                }
                adjustZoomLevelForRadius(selectedDistance)
            }
            //clearAllMarkers()
        }

        locationSensor.location.observe(this, Observer { location ->
            Log.d("MapDebug", "Observed location update: $location")
            lastKnownLocation = location
            if (!userMovedMap && !initialZoomDone && intent.getDoubleExtra("shelter_lat", 0.0) == 0.0) {
                updateLocationUI(location)
                drawCircle(selectedDistance)
                initialZoomDone = true

                // 現在地にズームされたらロード画面を非表示にする
                hideLoadingScreen()
            }
        })



        if (checkLocationPermission()) {
            requestLocationSetting()
            locationSensor.start()
        } else {
            requestLocationPermission()
        }

        // Initialize Toolbar and Drawer
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.title = "避難場所の件数: ー 件"

        drawerLayout = findViewById(R.id.drawer_layout)
        toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // NavigationView の menuItem を取得する
        navigationView = findViewById(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_saveshelter -> {
                    // 保存された避難場所を表示
                   // setupSavedSheltersListView()
                    val intent = Intent(this@MainActivity, SavedSheltersActivity::class.java)
                    startActivity(intent)
                    // データベースの内容をログに出力
                    logDatabaseContents()
                }

                R.id.nav_search -> {
                    // SearchShelterActivity への遷移処理
                    val intent = Intent(this@MainActivity, SearchShelterActivity::class.java)
                    //startActivity(intent)
                    startActivityForResult(intent, REQUEST_CODE_SEARCH_SHELTER)
                }
            }
            // アイテムが選択された後にドロワーを閉じる
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        if (checkLocationPermission() && ::mMap.isInitialized) {
        }

        /*fun saveShelter(shelter: SavedShelter) {
            lifecycleScope.launch {
                SavedShelterDatabase.getDatabase(this@MainActivity).savedShelterDao().insert(shelter)
            }
        }*/

        // マップの設定などの時間のかかる処理を非同期で行う
        // 完了後にロード画面を非表示にする
        /*Handler(Looper.getMainLooper()).postDelayed({
            hideLoadingScreen()
        }, 3000) // 待ってから非表示にする例*/

    }

    private fun performSearch(query: String) {
        clearAllMarkers() // ピンをクリア

        val shelters = ShelterHelper.searchShelters(query)
        updateSheltersUI(shelters)
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }


    @SuppressLint("PotentialBehaviorOverride")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // マップの初期設定
        val japanCenter = LatLng(35.682839, 139.759455)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(japanCenter, 5f))

        //isInitialZoomCompleted = true

        mMap.uiSettings.isZoomControlsEnabled = true


        // マップの設定が完了したらロード画面を非表示にする
        //hideLoadingScreen()

        // マーカーを追加（例としていくつかのマーカーを追加）
        //addMarkers()

        // マーカーの長押しリスナーを設定
        mMap.setOnMapLongClickListener { latLng ->
            Log.d("MapLongClick", "マップの長押しが検出されました: $latLng")

            // 長押しされた場所に最も近いマーカーを見つける
            val closestMarker = getClosestMarker(latLng)
            if (closestMarker != null) {
                Log.d("MapLongClick", "近くのマーカー: ${closestMarker.title}, position: ${closestMarker.position}")

                // 長押しされた位置がマーカーの近くならダイアログを表示
                if (isWithinMarkerProximity(latLng, closestMarker)) {
                    Log.d("MapLongClick", "長押しされた位置がマーカーの近くです。ダイアログを表示します。")
                    showSaveDialog(closestMarker)
                } else {
                    Log.d("MapLongClick", "長押しされた位置がマーカーの近くではありません。")
                }
            } else {
                Log.d("MapLongClick", "近くにマーカーが見つかりませんでした")
            }
        }

        // Intentから緯度と経度を取得し、マップを更新
        val lat = intent.getDoubleExtra("shelter_lat", 0.0)
        val lon = intent.getDoubleExtra("shelter_lon", 0.0)
        val shelterName = intent.getStringExtra("shelter_name")
        if (lat != 0.0 && lon != 0.0) {
            val shelterLocation = LatLng(lat, lon)
            mMap.clear() // 既存のマーカーをクリア
            mMap.addMarker(MarkerOptions().position(shelterLocation).title(shelterName ?: "Selected Shelter"))
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(shelterLocation, 15f)) // ズームレベルを調整
            initialZoomDone = true
            hideLoadingScreen()
        }

        if (checkLocationPermission()) {
            enableLocationFeatures()
        } else {
            requestLocationPermission()
        }

    }


    private fun showLoadingScreen() {
        if (!isInitialZoomCompleted) {
            Log.d("LoadingScreen", "Showing loading screen")
            runOnUiThread {
                loadingScreen.visibility = View.VISIBLE
                loadingScreen.bringToFront()
                loadingScreen.alpha = 0f
                loadingScreen.animate().alpha(1f).setDuration(100).start()
                loadingScreen.invalidate()
                Log.d("LoadingScreen", "Loading screen visibility: ${loadingScreen.visibility}")
            }
        }
    }

    private fun hideLoadingScreen() {
        Log.d("LoadingScreen", "Hiding loading screen")
        runOnUiThread {
            loadingScreen.animate().alpha(0f).setDuration(100).withEndAction {
                loadingScreen.visibility = View.GONE
                Log.d("LoadingScreen", "Loading screen visibility: ${loadingScreen.visibility}")
            }.start()
        }
    }




    // マーカーのリストから指定された位置に最も近いマーカーを返す
    private fun getClosestMarker(latLng: LatLng): Marker? {
        var closestMarker: Marker? = null
        var minDistance = Float.MAX_VALUE

        for (marker in shelterMarkers) {
            val distance = distanceBetween(latLng, marker.position)
            Log.d("DistanceCalculation", "距離: $distance, マーカー: ${marker.title}")

            if (distance < minDistance) {
                minDistance = distance
                closestMarker = marker
            }
        }
        return closestMarker
    }

    // 位置間の距離を計算する
    private fun distanceBetween(latLng1: LatLng, latLng2: LatLng): Float {
        val results = FloatArray(1)
        Location.distanceBetween(
            latLng1.latitude, latLng1.longitude,
            latLng2.latitude, latLng2.longitude,
            results
        )
        Log.d("DistanceBetween", "Distance between $latLng1 and $latLng2: ${results[0]} meters")
        return results[0]
    }

    // 指定された位置がマーカーの近くかどうかを判定する
    private fun isWithinMarkerProximity(latLng: LatLng, marker: Marker): Boolean {
        val proximityRadius = 50 // プロキシミティの距離（メートル単位）を調整
        val distance = distanceBetween(latLng, marker.position)
        val isWithin = distance <= proximityRadius
        Log.d("ProximityCheck", "Distance to marker ${marker.title}: $distance meters, is within proximity: $isWithin")
        return isWithin
    }

    // ダイアログを表示するメソッド
    private fun showSaveDialog(marker: Marker) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(marker.title)
        builder.setMessage("このマーカーを保存しますか？")

        builder.setPositiveButton("保存") { dialog, _ ->
            // 保存ボタンがクリックされたときの処理
            Toast.makeText(this, "${marker.title} を保存しました", Toast.LENGTH_SHORT).show()
            saveShelter(marker) // 保存処理を呼び出す
            dialog.dismiss()
        }

        builder.setNegativeButton("キャンセル") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()

        // ダイアログが表示された後にボタンの色を変更
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(resources.getColor(android.R.color.black))
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(resources.getColor(android.R.color.black))
        }

        dialog.show()
    }


// その他のメソッドはそのまま


    // 避難場所を保存するメソッド
    private fun saveShelter(marker: Marker) {
        // 保存処理を行う（データベースなどに保存するコードを記述）
        val name = marker.title ?: "不明な避難所"
        val position = marker.position
        val latitude = position.latitude
        val longitude = position.longitude

        lifecycleScope.launch {
            // 逆ジオコーディングを使用して住所を取得
            val address = getAddressFromLatLng(latitude, longitude)

            // 現在の最大IDを取得
            val maxId = SavedShelterDatabase.getDatabase(this@MainActivity).savedShelterDao().getMaxId() ?: 0
            val newId = maxId + 1

            val savedShelter = SavedShelter(
                id = newId,
                name = name,
                lat = latitude,
                lon = longitude,
                address = address ?: "住所が見つかりませんでした" // アドレスを適切に設定する
            )
            try {
            SavedShelterDatabase.getDatabase(this@MainActivity).savedShelterDao().insert(savedShelter)
                // `savedSheltersAdapter`が初期化されていることを確認
                if (::savedSheltersAdapter.isInitialized) {
                    loadSavedShelters() // 保存後にリストを更新
                    Log.d("SaveShelter", "Shelter saved: $savedShelter")
                } else {
                    Log.e("SaveShelter", "Error: savedSheltersAdapter is not initialized")
                }
            } catch (e: Exception) {
                Log.e("SaveShelter", "Error saving shelter: ${e.message}")
            }
        }
    }

    // 逆ジオコーディングを使用して緯度と経度から住所を取得する関数
    private fun getAddressFromLatLng(lat: Double, lon: Double): String? {
        val geocoder = Geocoder(this, Locale.getDefault())
        return try {
            val addresses = geocoder.getFromLocation(lat, lon, 1)
            if (addresses != null && addresses.isNotEmpty()) {
                val address = addresses[0]
                address.getAddressLine(0) // 住所の最初の行を取得
            } else {
                null
            }
        } catch (e: IOException) {
            Log.e("Geocoder", "Geocoder failed", e)
            null
        }
    }

    private fun updateLocationUI(location: Location) {
        val currentLatLng = LatLng(location.latitude, location.longitude)
        if (!initialZoomDone) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
        }
    }

    private fun updateSheltersUI(shelters: List<Shelter>) {
        Log.d(TAG, "Updating shelters UI with ${shelters.size} shelters")
        // 既存のマーカーを全て削除
        shelterMarkers.forEach { it.remove() }
        shelterMarkers.clear()

        // 新しいマーカーを追加
        shelters.forEach { shelter ->
            // デフォルトの赤色のマーカーを設定
            val markerOptions = MarkerOptions()
                .position(LatLng(shelter.latitude, shelter.longitude))
                .title(shelter.name) // タイトルは避難所の名前にする

            // マーカーをマップに追加し、shelterMarkers リストに保存
            mMap.addMarker(markerOptions)?.let { marker ->
                shelterMarkers.add(marker)
                Log.d(TAG, "Added marker: ${marker.title}, position: ${marker.position}")
            }
        }

        // アクションバーのタイトルに避難場所の件数を表示
        supportActionBar?.title = "避難場所の件数: ${shelters.size} 件"

        Log.d(TAG, "Current shelterMarkers list:")
        shelterMarkers.forEach { marker ->
            Log.d(TAG, "Marker: ${marker.title}, position: ${marker.position}")
        }

    }

    private fun clearAllMarkers() {
        // マップ上の全ての要素をクリア
        mMap.clear()
        // リスト内のマーカーもクリア
        shelterMarkers.clear()
    }

    private fun drawCircle(radius: Float) {
        currentCircle?.remove()
        lastKnownLocation?.let {
            val circleOptions = CircleOptions()
                .center(LatLng(it.latitude, it.longitude))
                .radius(radius.toDouble())
                .strokeWidth(2f)
                .strokeColor(0xFF0000FF.toInt())
                .fillColor(0x220000FF.toInt())
            //mMap.addCircle(circleOptions)
            currentCircle = mMap.addCircle(circleOptions)
            adjustZoomLevelForRadius(radius)
        }
    }

    private fun adjustZoomLevelForRadius(radius: Float) {
        val zoomLevel = when (radius) {
            in 0f..1000f -> 14.5f  // 1km
            in 1000f..2000f -> 13.5f  // 2km
            in 2000f..3000f -> 12.9f  // 3km
            in 3000f..4000f -> 12.5f  // 4km
            in 4000f..5000f -> 12.2f  // 5km
            else -> 10f  // デフォルトのズームレベル
        }
        lastKnownLocation?.let {
            val currentLatLng = LatLng(it.latitude, it.longitude)
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, zoomLevel))
        }
    }

    private fun checkLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    private fun requestLocationSetting() {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        val request = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .setAlwaysShow(true)
            .build()

        val result = LocationServices.getSettingsClient(this).checkLocationSettings(request)

        result.addOnCompleteListener { task ->
            try {
                task.getResult(ApiException::class.java)
            } catch (exception: ApiException) {
                when (exception.statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                        if (exception is ResolvableApiException) {
                            try {
                                exception.startResolutionForResult(
                                    this@MainActivity,
                                    REQUEST_CODE_LOCATION_SETTING
                                )
                            } catch (e: SendIntentException) {
                                Log.e("MainActivity", "Error starting resolution for result", e)
                            }
                        }
                    }

                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                        Log.e(
                            "MainActivity",
                            "Location settings are inadequate, and cannot be fixed here. Fix in Settings."
                        )
                    }
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // 権限が許可された場合、位置情報の設定をリクエストしてセンサーを開始
                if (checkLocationPermission()) {
                    requestLocationSetting()
                    locationSensor.start()
                    // マップが初期化されている場合にのみ位置情報機能を有効にする
                    if (::mMap.isInitialized) {
                        enableLocationFeatures()
                    }
                }
            } else {
                Toast.makeText(this, "位置情報の許可が必要です", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun enableLocationFeatures() {
        if (checkLocationPermission()) {
            mMap.isMyLocationEnabled = true
            mMap.uiSettings.isZoomGesturesEnabled = true
            mMap.uiSettings.isMyLocationButtonEnabled = true

            val toolbarHeight = resources.getDimensionPixelSize(R.dimen.toolbar_height)
            mMap.setPadding(0, toolbarHeight, 0, 0)
        } else {
            mMap.isMyLocationEnabled = false
            mMap.uiSettings.isZoomGesturesEnabled = false
            mMap.uiSettings.isMyLocationButtonEnabled = false

            Toast.makeText(this, "位置情報の権限が必要です", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_SEARCH_SHELTER && resultCode == Activity.RESULT_OK) {
            val selectedShelter = data?.getParcelableExtra<Shelter>("selectedShelter")

            selectedShelter?.let {
                clearAllMarkers()
                // 選択された避難場所の位置情報を取得してマップ上にピンを追加する
                val location = LatLng(it.latitude, it.longitude)
                val markerOptions = MarkerOptions()
                    .position(location)
                    .title(it.name)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)) // 青色のマーカーを設定
                val marker = mMap.addMarker(markerOptions)
                marker?.let { addedMarker ->
                    shelterMarkers.add(addedMarker) // マーカーをリストに追加
                }
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
            }
            supportActionBar?.title = "避難場所の件数: 1 件"
        }
    }


    // データを取得してアダプターにセット
    private fun loadSavedShelters() {
        savedShelterDao.getAllSavedShelters().observe(this) { savedShelters ->
            Log.d("LoadSavedShelters", "Loaded shelters: $savedShelters")
            // データが更新されたらアダプターに反映
            savedSheltersAdapter.updateData(savedShelters)
        }
    }

    private fun deleteShelter(shelter: SavedShelter) {
        lifecycleScope.launch {
            SavedShelterDatabase.getDatabase(this@MainActivity).savedShelterDao().delete(shelter)
            Log.d("LoadSavedShelters", "Loaded shelters: $ SavedShelter")
            loadSavedShelters()
        }
    }

    private fun logDatabaseContents() {
        lifecycleScope.launch {
            val liveData = savedShelterDao.getAllSavedShelters()
            liveData.observe(this@MainActivity) { savedShelters ->
                savedShelters?.forEach { shelter ->
                    Log.d("DatabaseContent", "Shelter: $shelter")
                }
            }
        }
    }



}