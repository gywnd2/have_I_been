package com.udangtangtang.haveibeen

import androidx.appcompat.app.AppCompatActivity
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Overlay
import com.naver.maps.map.util.FusedLocationSource
import com.naver.maps.map.NaverMap
import com.naver.maps.map.UiSettings
import com.naver.maps.map.overlay.InfoWindow
import com.udangtangtang.haveibeen.util.PictureScanHelper
import android.os.Bundle
import com.udangtangtang.haveibeen.util.PermissionHelper
import android.Manifest.permission
import android.content.Intent
import androidx.annotation.UiThread
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.overlay.InfoWindow.DefaultViewAdapter
import android.widget.TextView
import android.widget.RatingBar
import com.naver.maps.map.NaverMap.OnMapClickListener
import android.graphics.PointF
import androidx.core.app.ActivityCompat
import android.content.pm.PackageManager
import android.database.Cursor
import android.view.View
import com.naver.maps.map.LocationTrackingMode
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import com.naver.maps.map.overlay.Marker
import com.udangtangtang.haveibeen.databinding.ActivityMainBinding
import com.udangtangtang.haveibeen.model.*
import java.util.ArrayList

class MainActivity : AppCompatActivity(), OnMapReadyCallback, Overlay.OnClickListener {
    private lateinit var pictureDB:PictureDatabase
    private lateinit var binding: ActivityMainBinding
    private lateinit var mLocationSource: FusedLocationSource
    private lateinit var mNaverMap: NaverMap
    private lateinit var uiSettings: UiSettings
    private lateinit var mInfoWindow: InfoWindow
    private var pictureList=listOf<PictureEntity>()
    private val permissionHelper=PermissionHelper()
    private lateinit var markers: Array<Marker?>
    private lateinit var selectedLatLng: Array<String?>
    private lateinit var pictureScanHelper: PictureScanHelper
    private var backKeyTime: Long = 0
    private var dbSize = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setTitle(R.string.app_name)

        // 외부 저장소 권한 요청
        permissionHelper.checkPermission(
            this,
            permission.READ_EXTERNAL_STORAGE,
            REQ_PERMISSION_CALLBACK
        )

        // 권한이 있다면 DB 초기화 및 사진 스캔
        if (permissionHelper.isPermissionGranted(this, permission.READ_EXTERNAL_STORAGE)){
            // DB 초기화
            pictureDB=PictureDatabase.getInstance(this)!!

            // 사진 스캔
            pictureScanHelper = PictureScanHelper(this)
            pictureScanHelper!!.scanPictures()

            // DB에 이미지 파일 추가하고 좌표 있는 이미지 개수 받아오기
//            pictureScanHelper!!.initializePictureDB(dbHelper, pictureList)
//            dbSize = dbHelper!!.sizeOfPictureDB
        }

        // mapView 초기화
        binding.mainMapView.getMapAsync(this)
        mLocationSource = FusedLocationSource(this, PERMISSION_REQUEST_CODE)

        // Floating Button (Settings)
        binding.mainFabSettings.setOnClickListener(View.OnClickListener {
            val intent = Intent(applicationContext, SettingActivity::class.java)
            startActivity(intent)
        })

//         Floating Button (Ranking), 클릭 시 랭킹 액티비티 전환
        binding.mainFabRanking.setOnClickListener(View.OnClickListener {
            val intent = Intent(applicationContext, RankingActivity::class.java)
            startActivity(intent)
        })
    }

    @UiThread
    override fun onMapReady(naverMap: NaverMap) {
//        // DB로부터 마커 추가
//        if (permissionHelper.isPermissionGranted(this, permission.READ_EXTERNAL_STORAGE)) {
//            var markerIdx = 0
//            val cursor: Cursor
//            markers = arrayOfNulls(dbSize)
//            val sqlDB = dbHelper!!.readableDatabase
//            cursor = sqlDB.rawQuery("select distinct latitude, longtitude from myDB", null)
//            while (cursor != null && cursor.moveToNext()) {
//                markers[markerIdx] = Marker()
//                markers[markerIdx]!!.position = LatLng(cursor.getDouble(0), cursor.getDouble(1))
//                markers[markerIdx]!!.map = naverMap
//
//                // 마커 클릭 이벤트
//                markers[markerIdx]!!.onClickListener = this
//                markerIdx += 1
//            }
//
//            // DB연결 종료
//            sqlDB.close()
//        }
//
//        // 마커 정보창 생성
//        mInfoWindow = InfoWindow()
//
//        // 정보창 어댑터 설정
//        mInfoWindow!!.adapter = object : DefaultViewAdapter(this) {
//            override fun getContentView(infoWindow: InfoWindow): View {
//                // 선택한 정보창에 해당하는 마커 객체 가져오기
//                val marker = infoWindow.marker
//
//                // 인텐트로 상세조회 페이지에 넘겨주기 위한 위/경도 기록
//                selectedLatLng = arrayOf(
//                    marker!!.position.latitude.toString(),
//                    marker.position.longitude.toString()
//                )
//
//                // 위/경도로 정보창 데이터 가져오기
//                infoWindowData = InfoWindowData()
//                infoWindowData = dbHelper!!.getInfoWindowData(selectedLatLng)
//
//                // 가져온 데이터로 뷰 만들기
//                val view = View.inflate(this@MainActivity, R.layout.marker_infowindow, null)
//                (view.findViewById<View>(R.id.infoWindow_locationTitle) as TextView).text =
//                    if (infoWindowData.locationName == null) getString(R.string.record_detail_no_locName) else infoWindowData.locationName
//                (view.findViewById<View>(R.id.infoWindow_locationAddress) as TextView).text =
//                    if (infoWindowData.address == null) getString(R.string.no_location_info) else infoWindowData.address
//                (view.findViewById<View>(R.id.infoWindow_datetime) as TextView).text =
//                    if (infoWindowData.datetime == null) getString(R.string.no_datetime_info) else infoWindowData.datetime
//                (view.findViewById<View>(R.id.infoWindow_comment) as TextView).text =
//                    if (infoWindowData.comment == null) getString(R.string.record_detail_no_comment) else infoWindowData.comment
//                (view.findViewById<View>(R.id.infoWindow_ratingBar) as RatingBar).rating =
//                    if (infoWindowData.rating
//                            .toDouble() == 0.0
//                    ) 0.0.toFloat() else infoWindowData.rating
//                return view
//            }
//        }

        // 정보창 클릭 이벤트
        mInfoWindow!!.onClickListener = Overlay.OnClickListener { // 정보창 클릭 시 정보 상세정보 확인 액티비티 전환
            val intent = Intent(binding!!.root.context, RecordDetailActivity::class.java)
            // 인텐트에 위/경도를 첨부해서 전달
            intent.putExtra("selectedLatLng", selectedLatLng)
            startActivity(intent)
            false
        }

        // 정보창이 아닌 지도 클릭 시 정보창 닫기
        naverMap.onMapClickListener =
            OnMapClickListener { coord: PointF?, point: LatLng? -> mInfoWindow!!.close() }

        // 지도 UI
        uiSettings = naverMap.uiSettings
        uiSettings!!.isCompassEnabled = true
        uiSettings!!.isLocationButtonEnabled = true

        // 위치 확인을 위한 locationSource와 권한 요청
        mNaverMap = naverMap
        naverMap.locationSource = mLocationSource
        ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_REQUEST_CODE)
    }

    // 지도 권한 요청 콜백 메소드
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.size > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                mNaverMap!!.locationTrackingMode = LocationTrackingMode.Follow
            }
        }
    }

    // 마커 클릭 이벤트
    override fun onClick(overlay: Overlay): Boolean {
        if (overlay is Marker) {
            val marker = overlay
            if (marker.infoWindow != null) {
                mInfoWindow!!.close()
            } else {
                mInfoWindow!!.open(marker)
            }
            return true
        }
        return false
    }

    // 뒤로가기 이벤트
    override fun onBackPressed() {
        if (System.currentTimeMillis() > backKeyTime + 2000) {
            backKeyTime = System.currentTimeMillis()
            Toast.makeText(this, getString(R.string.backkeypressed), Toast.LENGTH_LONG).show()
            return
        }
        if (System.currentTimeMillis() <= backKeyTime + 2000) {
            finish()
        }
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 100
        private val PERMISSIONS = arrayOf(
            permission.ACCESS_FINE_LOCATION,
            permission.ACCESS_COARSE_LOCATION
        )

        // 권한 요청에 대한 응답 코드
        private const val REQ_PERMISSION_CALLBACK = 100
    }
}