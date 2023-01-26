package com.udangtangtang.haveibeen

import android.Manifest.permission.*
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.PointF
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.ViewManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.annotation.UiThread
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.NaverMap
import com.naver.maps.map.NaverMap.OnMapClickListener
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.UiSettings
import com.naver.maps.map.overlay.InfoWindow
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.Overlay
import com.naver.maps.map.util.FusedLocationSource
import com.udangtangtang.haveibeen.databinding.ActivityMainBinding
import com.udangtangtang.haveibeen.databinding.MarkerInfowindowBinding
import com.udangtangtang.haveibeen.repository.RecordRepository
import com.udangtangtang.haveibeen.util.PictureScanHelper
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity(), OnMapReadyCallback, Overlay.OnClickListener {
    // TODO : DB 코루틴, 초기 실행 시 사진 스캔(로딩화면?), 사진 변경 감지 어떻게?
    private lateinit var binding: ActivityMainBinding
    private lateinit var infoWindowBinding : MarkerInfowindowBinding
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
                        edit().apply()
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
        binding.mainFabSettings.setOnClickListener{
            startActivity(Intent(this, SettingActivity::class.java))
        }

//         Floating Button (Ranking), 클릭 시 랭킹 액티비티 전환
        binding.mainFabRanking.setOnClickListener{
            startActivity(Intent(this, RankingActivity::class.java))
        }
    }

    @UiThread
    override fun onMapReady(naverMap: NaverMap) {
        // 마커 정보창 생성
        mInfoWindow = InfoWindow()
        infoWindowBinding = DataBindingUtil.inflate<MarkerInfowindowBinding>(layoutInflater, R.layout.marker_infowindow, binding.root, false)
        var selectedLatLng= DoubleArray(2)

        // 정보창 어댑터 설정
        mInfoWindow.adapter = object : InfoWindow.DefaultViewAdapter(this) {
            override fun getContentView(p0: InfoWindow): View {
                // 선택한 정보창에 해당하는 마커 객체 가져오기
                val marker = p0.marker

                // 인텐트로 상세조회 페이지에 넘겨주기 위한 위/경도 기록
                selectedLatLng[0]=marker!!.position.latitude
                selectedLatLng[1]= marker.position.longitude

                // 가져온 데이터로 뷰 만들기
                val record=db.getRecord(selectedLatLng.get(0), selectedLatLng.get(1))
                // TODO: Fix
                with(infoWindowBinding){
                    infoWindowLocationTitle.text=record.locationName
                    if (record.rating==null) infoWindowRatingBar.rating=getString(R.string.no_rating).toFloat()
                    else infoWindowRatingBar.rating=record.rating!!
                    infoWindowLocationAddress.text=record.address
                    infoWindowDatetime.text=record.datetime
                    if (record.comment==null) infoWindowComment.text=getString(R.string.no_comment)
                    else infoWindowComment.text=record.comment
                }

                Log.d(TAG, infoWindowBinding.infoWindowData.toString())
                return infoWindowBinding.root
            }
        }

        // 정보창 클릭 이벤트
        mInfoWindow.onClickListener = Overlay.OnClickListener { // 정보창 클릭 시 정보 상세정보 확인 액티비티 전환
            val intent = Intent(this, RecordDetailActivity::class.java)
            // 인텐트에 위/경도를 첨부해서 전달
            intent.putExtra("selectedLatLng", selectedLatLng)
            startActivity(intent)
            false
        }

        // DB로부터 마커 추가
        Log.d(TAG, db.getTotalPictureCount().toString())
        val pictureCount=db.getTotalPictureCount()
        val pictureList=db.getPictureList()
        if (pictureCount>0) {
            val markers = mutableListOf<Marker>()
            for (i in 0 until pictureCount){
                val marker=Marker()
                val latLng=db.getPictureCoordination(pictureList.get(i))
                Log.d(TAG, "Add Marker at :"+ latLng.latitude+"/"+latLng.longtitude)
                marker.position=LatLng(latLng.latitude, latLng.longtitude)
                marker.map=naverMap
                // 마커 클릭 이벤트
                marker.onClickListener=this
                markers.add(i, marker)
            }

        }

        // 정보창이 아닌 지도 클릭 시 정보창 닫기
        naverMap.onMapClickListener =
            OnMapClickListener { coord: PointF?, point: LatLng? -> mInfoWindow.close() }

        // 지도 UI
        uiSettings = naverMap.uiSettings
        uiSettings.isCompassEnabled = true
        uiSettings.isLocationButtonEnabled = true

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
            if (grantResults.isNotEmpty()
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                mNaverMap.locationTrackingMode = LocationTrackingMode.Follow
            }
        }
    }

    // 마커 클릭 이벤트
    override fun onClick(overlay: Overlay): Boolean {
        if (overlay is Marker) {
            if (overlay.infoWindow != null) {
                mInfoWindow.close()
            } else {
//                infoWindowBinding.infoWindowData=db.getRecord(overlay.position.latitude, overlay.position.longitude)
                mInfoWindow.open(overlay)
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