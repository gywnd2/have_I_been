package com.udangtangtang.haveibeen.activity

import android.Manifest.permission.*
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.PointF
import android.icu.text.IDNA.Info
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.service.notification.NotificationListenerService
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.annotation.UiThread
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.view.marginLeft
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.android.material.snackbar.Snackbar
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
import com.udangtangtang.haveibeen.R
import com.udangtangtang.haveibeen.databinding.ActivityMainBinding
import com.udangtangtang.haveibeen.databinding.MarkerInfowindowBinding
import com.udangtangtang.haveibeen.entity.InfoWindowData
import com.udangtangtang.haveibeen.entity.RecordEntity
import com.udangtangtang.haveibeen.fragment.RecordViewFragment
import com.udangtangtang.haveibeen.repository.RecordRepository
import com.udangtangtang.haveibeen.util.GeocodingHelper
import com.udangtangtang.haveibeen.util.PictureScanHelper
import com.udangtangtang.haveibeen.util.RankingCardAdapter
import com.udangtangtang.haveibeen.viewmodel.InfoWindowViewModel
import kotlinx.coroutines.*
import kotlin.math.roundToInt

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
    private lateinit var db : RecordRepository
    private lateinit var sharedPreferences: SharedPreferences
    public lateinit var infoWindowData : LiveData<InfoWindowData>

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
        supportActionBar?.elevation=0.0f

        // Init
        db= RecordRepository(application)
        sharedPreferences= EncryptedSharedPreferences.create(
            this,
            FILENAME,
            MasterKey.Builder(this).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        // 사진 스캔
        pictureScanHelper = PictureScanHelper(this, db)
        val gen=MediaStore.getGeneration(applicationContext, MediaStore.VOLUME_EXTERNAL).toString()
        with(sharedPreferences){
            if(getString(MEDIASTORE_GEN_ATTR, "null")!=gen) {
                binding.containerMainScanProgress.visibility=View.VISIBLE
                binding.containerMainScanProgress.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.anim_slide_down))
                    CoroutineScope(Dispatchers.Main).launch {
                        async {
                            edit().putString(MEDIASTORE_GEN_ATTR, gen)
                            edit().apply()
                            pictureScanHelper.scanPictures()
                            delay(5000L)
                        }.await()
                        binding.containerMainScanProgress.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.anim_slide_up))
                        binding.containerMainScanProgress.visibility = View.GONE
                    }
                }
            }
        Log.d(TAG, "dialog close")

        Toast.makeText(this, MediaStore.getVersion(this, MediaStore.VOLUME_EXTERNAL), Toast.LENGTH_LONG).show()

        // mapView 초기화
        binding.mainMapView.getMapAsync(this)
        mLocationSource = FusedLocationSource(this, PERMISSION_REQUEST_CODE)

        // Floating Button (Settings)
        binding.mainFabSettings.setOnClickListener{
            startActivity(Intent(this, SettingActivity::class.java))
        }

//         Floating Button (NotificationListenerService.Ranking), 클릭 시 랭킹 액티비티 전환
        binding.mainFabRanking.setOnClickListener{
            startActivity(Intent(this, RankingActivity::class.java))
        }

        // Viewpager
        binding.mainViewpager.adapter=RankingCardAdapter(this, db)
    }

    @UiThread
    override fun onMapReady(naverMap: NaverMap) {
        // Naver logo
        naverMap.uiSettings.setLogoMargin(30, 0, 0, binding.mainContainerViewpager.height)

        // 마커 정보창 생성
        mInfoWindow = InfoWindow()
        infoWindowBinding = DataBindingUtil.inflate(layoutInflater, R.layout.marker_infowindow, binding.root, false)
        var selectedLatLng= DoubleArray(2)

        // 정보창 어댑터 설정
        mInfoWindow.adapter = object : InfoWindow.DefaultViewAdapter(this) {
            @RequiresApi(Build.VERSION_CODES.TIRAMISU)
            override fun getContentView(p0: InfoWindow): View {
                // 선택한 정보창에 해당하는 마커 객체 가져오기
                val marker = p0.marker

                // 인텐트로 상세조회 페이지에 넘겨주기 위한 위/경도 기록
                selectedLatLng[0] = marker!!.position.latitude
                selectedLatLng[1] = marker.position.longitude

                // 가져온 데이터로 뷰 만들기
                CoroutineScope(Dispatchers.Main).launch {
                    val factory= InfoWindowViewModel.InfoWindowViewModelFactory(db, selectedLatLng)
                    val infoWindowViewModel=ViewModelProvider(this@MainActivity, factory).get(InfoWindowViewModel::class.java)
                    infoWindowBinding.viewModel=infoWindowViewModel

                    val infoWindowDataObserver=object :Observer<InfoWindowData>{
                        override fun onChanged(t: InfoWindowData?) {
                            Log.d(TAG, t.toString())
                            if(t!=null){
                                infoWindowViewModel.setInfoWindow(t)
                                if(infoWindowViewModel.currentRecord.value!!.address==null){
                                    infoWindowBinding.infoWindowLocationAddress.text=getString(R.string.no_address)
                                }
                                Log.d(TAG, "InfoWindow changed : "+t.toString())
                            }
                        }
                    }

                    infoWindowViewModel.currentRecord.observe(this@MainActivity, infoWindowDataObserver)
                }
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
        CoroutineScope(Dispatchers.Main).launch {
            Log.d(TAG, "dbcount : "+db.getTotalPictureCount().toString())
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
                    marker.onClickListener=this@MainActivity
                    markers.add(i, marker)
                }

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
            Snackbar.make(binding.root, getString(R.string.backkeypressed), Snackbar.LENGTH_LONG)
            return
        }
        if (System.currentTimeMillis() <= backKeyTime + 2000) {
            finish()
        }
    }

    @SuppressLint("SetTextI18n")
    fun updateProgress(progressInFloat:Float){
        binding.viewInitscanProgressbar.progress= (progressInFloat*100).roundToInt()
        binding.viewInitscanProgressText.text=(progressInFloat*100).roundToInt().toString()+"%"
    }
}