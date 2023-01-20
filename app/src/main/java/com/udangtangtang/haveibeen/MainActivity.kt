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
import com.naver.maps.map.NaverMap.OnMapClickListener
import android.graphics.PointF
import androidx.core.app.ActivityCompat
import android.content.pm.PackageManager
import android.database.Cursor
import android.util.Log
import android.view.View
import com.naver.maps.map.LocationTrackingMode
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.core.content.ContextCompat
import com.naver.maps.map.overlay.Marker
import com.udangtangtang.haveibeen.databinding.ActivityMainBinding
import com.udangtangtang.haveibeen.databinding.MarkerInfowindowBinding
import com.udangtangtang.haveibeen.entity.RecordEntity
import com.udangtangtang.haveibeen.database.PictureDatabase
import com.udangtangtang.haveibeen.database.RecordDatabase
import java.util.ArrayList

class MainActivity : AppCompatActivity(), OnMapReadyCallback, Overlay.OnClickListener {
    private lateinit var pictureDB: PictureDatabase
    private lateinit var recordDB: RecordDatabase
    private lateinit var binding: ActivityMainBinding
    private lateinit var mLocationSource: FusedLocationSource
    private lateinit var mNaverMap: NaverMap
    private lateinit var uiSettings: UiSettings
    private lateinit var mInfoWindow: InfoWindow
    private var pictureList=ArrayList<String>()
    private val permissionHelper=PermissionHelper()
    private lateinit var markers: ArrayList<Marker?>
    private lateinit var selectedLatLng: Array<Double>
    private lateinit var pictureScanHelper: PictureScanHelper
    private var backKeyTime: Long = 0
    private var dbSize = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setTitle(R.string.app_name)

        // 외부 저장소 권한 요청
        val requestPermissionLauncher=registerForActivityResult(RequestPermission()){
            isGranted: Boolean->
                if(isGranted){
                    Toast.makeText(this, "권한 획득", Toast.LENGTH_LONG).show()
                }else{
                    Toast.makeText(this, "권한 획득 실패", Toast.LENGTH_LONG).show()
                }
        }

        // READ_MEDIA_IMAGES 권한 획득
        when {
            ContextCompat.checkSelfPermission(
                this, android.Manifest.permission.READ_MEDIA_IMAGES)==PackageManager.PERMISSION_GRANTED->{
                pictureDB= PictureDatabase.getInstance(this)!!
                recordDB= RecordDatabase.getInstance(this)!!

                // 사진 스캔
                // ACCESS_MEDIA_LOCATION 권한 획득
                when{
                    ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_MEDIA_LOCATION)==PackageManager.PERMISSION_GRANTED->{
                        Toast.makeText(this, "사진 스캔 시작", Toast.LENGTH_LONG).show()
                        pictureScanHelper = PictureScanHelper(this)
                        pictureScanHelper!!.scanPictures()
                    }
                    else->{
                        requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_MEDIA_LOCATION)
                    }
                }

            }
            else->{
                requestPermissionLauncher.launch(
                    android.Manifest.permission.READ_MEDIA_IMAGES)
            }
        }


//        permissionHelper.checkPermission(
//            this,
//            permission.READ_EXTERNAL_STORAGE,
//            REQ_PERMISSION_CALLBACK
//        )
//        Toast.makeText(this, permissionHelper.checkPermissionGranted(this, permission.READ_EXTERNAL_STORAGE).toString(), Toast.LENGTH_LONG).show()
////        초기화 및 사진 스캔
//        if (permissionHelper.checkPermissionGranted(this, permission.READ_EXTERNAL_STORAGE)){
////             DB 초기화
//            pictureDB= PictureDatabase.getInstance(this)!!
//            recordDB= RecordDatabase.getInstance(this)!!
//
//            // 사진 스캔
//            pictureScanHelper = PictureScanHelper(this)
//            pictureScanHelper!!.scanPictures()
//            Toast.makeText(this, pictureDB.getPictureDao().getPictureNumbers().toString(), Toast.LENGTH_LONG).show()
//
//        }

        // mapView 초기화
        binding.mainMapView.getMapAsync(this)
        mLocationSource = FusedLocationSource(this, PERMISSION_REQUEST_CODE)

        // Floating Button (Settings)
//        binding.mainFabSettings.setOnClickListener(View.OnClickListener {
//            val intent = Intent(applicationContext, SettingActivity::class.java)
//            startActivity(intent)
//        })

//         Floating Button (Ranking), 클릭 시 랭킹 액티비티 전환
        binding.mainFabRanking.setOnClickListener(View.OnClickListener {
            val intent = Intent(applicationContext, RankingActivity::class.java)
            startActivity(intent)
        })
    }

    @UiThread
    override fun onMapReady(naverMap: NaverMap) {
        // DB로부터 마커 추가
        if (permissionHelper.checkPermissionGranted(this, permission.READ_EXTERNAL_STORAGE)) {
            var markerIdx = 0
            val cursor: Cursor
            markers = arrayListOf<Marker?>()
            pictureList=pictureDB.getPictureDao().getFileList() as ArrayList<String>

            for (i in 0 until pictureDB.getPictureDao().getPictureNumbers()){
                markers[i]=Marker()
                val latLng=pictureDB.getPictureDao().getPictureLatLng(pictureList.get(i))
                markers[i]!!.position=LatLng(latLng[0], latLng[1])
                markers[i]!!.map=naverMap

                // 마커 클릭 이벤트
                markers[i]!!.onClickListener = this
            }

        }

        // 마커 정보창 생성
        mInfoWindow = InfoWindow()

        // 정보창 어댑터 설정
        mInfoWindow!!.adapter = object : InfoWindow.DefaultViewAdapter(this) {
            override fun getContentView(infoWindow: InfoWindow): View {
                // 선택한 정보창에 해당하는 마커 객체 가져오기
                val marker = infoWindow.marker

                // 인텐트로 상세조회 페이지에 넘겨주기 위한 위/경도 기록
                selectedLatLng = arrayOf(
                    marker!!.position.latitude,
                    marker!!.position.longitude
                )

                // 위/경도로 정보창 데이터 가져오기
//                val infoWindowData : InfoWindowD
//                infoWindowData = dbHelper!!.getInfoWindowData(selectedLatLng)

                // 가져온 데이터로 뷰 만들기
                val infowindowBinding = MarkerInfowindowBinding.inflate(layoutInflater)
                val record : RecordEntity =recordDB.getRecordDao().getRecord(selectedLatLng.get(0), selectedLatLng.get(1))
                if (record.locationName==null) infowindowBinding.infoWindowLocationTitle.text=getString(R.string.record_detail_no_locName) else infowindowBinding.infoWindowLocationTitle.text=record.locationName
                if (record.latitude==null || record.longtitude==null) infowindowBinding.infoWindowLocationAddress.text=getString(R.string.no_location_info) else infowindowBinding.infoWindowLocationAddress.text=record.address
                if (record.datetime==null) infowindowBinding.infoWindowDatetime.text=getString(R.string.no_datetime_info) else infowindowBinding.infoWindowDatetime.text=record.datetime
                if (record.comment==null) infowindowBinding.infoWindowComment.text=getString(R.string.record_detail_no_comment) else infowindowBinding.infoWindowComment.text=record.comment
                if (record.rating==null) infowindowBinding.infoWindowRatingBar.rating=0.0.toFloat() else infowindowBinding.infoWindowRatingBar.rating=
                    record.rating!!
                return infowindowBinding.root
            }
        }

        // 정보창 클릭 이벤트
//        mInfoWindow!!.onClickListener = Overlay.OnClickListener { // 정보창 클릭 시 정보 상세정보 확인 액티비티 전환
//            val intent = Intent(binding!!.root.context, RecordDetailActivity::class.java)
//            // 인텐트에 위/경도를 첨부해서 전달
//            intent.putExtra("selectedLatLng", selectedLatLng)
//            startActivity(intent)
//            false
//        }

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