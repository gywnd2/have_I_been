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
import android.Manifest.permission
import android.Manifest.permission.*
import android.content.Intent
import android.content.SharedPreferences
import androidx.annotation.UiThread
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.NaverMap.OnMapClickListener
import android.graphics.PointF
import androidx.core.app.ActivityCompat
import android.content.pm.PackageManager
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.view.View
import com.naver.maps.map.LocationTrackingMode
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.naver.maps.map.overlay.Marker
import com.udangtangtang.haveibeen.databinding.ActivityMainBinding
import com.udangtangtang.haveibeen.databinding.MarkerInfowindowBinding
import com.udangtangtang.haveibeen.repository.RecordRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO

class MainActivity : AppCompatActivity(), OnMapReadyCallback, Overlay.OnClickListener {
    // TODO : 같은 장소 사진 처리, DB 코루틴, 초기 실행 시 사진 스캔(로딩화면?),
    private lateinit var binding: ActivityMainBinding
    private lateinit var mLocationSource: FusedLocationSource
    private lateinit var mNaverMap: NaverMap
    private lateinit var uiSettings: UiSettings
    private lateinit var mInfoWindow: InfoWindow
    private lateinit var pictureScanHelper: PictureScanHelper
    private var backKeyTime: Long = 0
    private val TAG="MainActivity"
    private lateinit var scanDialog : InitScanDialogFragment
    private lateinit var db : RecordRepository
    private lateinit var sharedPreferences: SharedPreferences

    companion object {
        private const val PERMISSION_REQUEST_CODE = 100
        // 권한 요청에 대한 응답 코드
        private const val REQ_PERMISSION_CALLBACK = 100
        private const val FILENAME="encrypted_pref"
        private const val MEDIASTORE_GEN_ATTR="mediastore_gen"
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setTitle(R.string.app_name)

        // Init
        db= RecordRepository(application)
        scanDialog=InitScanDialogFragment()
        sharedPreferences= EncryptedSharedPreferences.create(
            this,
            FILENAME,
            MasterKey.Builder(this).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )


        pictureScanHelper = PictureScanHelper(applicationContext)
        val gen=MediaStore.getGeneration(applicationContext, MediaStore.VOLUME_EXTERNAL).toString()
        with(sharedPreferences){
            if(getString(MEDIASTORE_GEN_ATTR, "null")!=gen){
                scanDialog.show(supportFragmentManager, "InitScanDialog")
                CoroutineScope(Dispatchers.IO).launch {
                    async{
                        edit().putString(MEDIASTORE_GEN_ATTR, gen)
                        pictureScanHelper.scanPictures()
                        }.await()
                    scanDialog.dismiss()
                    }
                }
            }
        Log.d(TAG, "dialog close")
        scanDialog.dismiss()

        Toast.makeText(this, MediaStore.getVersion(this, MediaStore.VOLUME_EXTERNAL), Toast.LENGTH_LONG).show()

        // mapView 초기화
        binding.mainMapView.getMapAsync(this)
        mLocationSource = FusedLocationSource(this, PERMISSION_REQUEST_CODE)

        // Floating Button (Settings)
        binding.mainFabSettings.setOnClickListener(View.OnClickListener {
            startActivity(Intent(this, SettingActivity::class.java))
        })

//         Floating Button (Ranking), 클릭 시 랭킹 액티비티 전환
        binding.mainFabRanking.setOnClickListener(View.OnClickListener {
            startActivity(Intent(this, RankingActivity::class.java))
        })
    }

    @UiThread
    override fun onMapReady(naverMap: NaverMap) {
        // DB로부터 마커 추가
        Log.d(TAG, db.getTotalPictureCount().toString())
        val pictureCount=db.getTotalPictureCount()
        val pictureList=db.getPictureList()
        if (pictureCount>0) {
            val markers = mutableListOf<Marker>()
            Log.d(TAG, markers.size.toString())
            for (i in 0 until pictureCount){
                val marker=Marker()
                val latLng=db.getPictureCoordination(pictureList.get(i))
                Log.d(TAG, "Add Marker at :"+ latLng.latitude+"/"+latLng.longtitude)
                marker.position=LatLng(latLng.latitude, latLng.longtitude)
                marker.map=naverMap

                // 마커 클릭 이벤트
                marker.onClickListener = this

                markers.add(i, marker)
            }

        }

        // 마커 정보창 생성
        mInfoWindow = InfoWindow()
        var selectedLatLng= mutableListOf<Double>()

        // 정보창 어댑터 설정
        mInfoWindow!!.adapter = object : InfoWindow.DefaultViewAdapter(this) {
            override fun getContentView(infoWindow: InfoWindow): View {
                // 선택한 정보창에 해당하는 마커 객체 가져오기
                val marker = infoWindow.marker

                // 인텐트로 상세조회 페이지에 넘겨주기 위한 위/경도 기록
                selectedLatLng.add(0, marker!!.position.latitude)
                selectedLatLng.add(1, marker!!.position.longitude)

                // 가져온 데이터로 뷰 만들기
                val infowindowBinding = MarkerInfowindowBinding.inflate(layoutInflater)
                val record=db.getRecord(selectedLatLng.get(0), selectedLatLng.get(1))
                Log.d(TAG, record.toString())
                if (record.locationName==null) infowindowBinding.infoWindowLocationTitle.text=getString(R.string.record_detail_no_locName) else infowindowBinding.infoWindowLocationTitle.text=record.locationName
                if (record.address==null) infowindowBinding.infoWindowLocationAddress.text=getString(R.string.no_location_info) else infowindowBinding.infoWindowLocationAddress.text=record.address
                if (record.datetime==null) infowindowBinding.infoWindowDatetime.text=getString(R.string.no_datetime_info) else infowindowBinding.infoWindowDatetime.text=record.datetime
                if (record.comment==null) infowindowBinding.infoWindowComment.text=getString(R.string.record_detail_no_comment) else infowindowBinding.infoWindowComment.text=record.comment
                if (record.rating==null) infowindowBinding.infoWindowRatingBar.rating=0.0.toFloat() else infowindowBinding.infoWindowRatingBar.rating=
                    record.rating!!
                return infowindowBinding.root
            }
        }

        // 정보창 클릭 이벤트
        mInfoWindow!!.onClickListener = Overlay.OnClickListener { // 정보창 클릭 시 정보 상세정보 확인 액티비티 전환
            val intent = Intent(this, RecordDetailActivity::class.java)
            // 인텐트에 위/경도를 첨부해서 전달
            intent.putExtra("selectedLatLng", selectedLatLng as DoubleArray)
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




}