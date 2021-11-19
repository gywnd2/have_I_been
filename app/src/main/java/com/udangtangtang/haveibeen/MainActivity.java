package com.udangtangtang.haveibeen;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;

import static com.udangtangtang.haveibeen.util.PermissionHelper.isPermissionGranted;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;

import androidx.core.app.ActivityCompat;

import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.UiSettings;
import com.naver.maps.map.overlay.InfoWindow;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.Overlay;
import com.naver.maps.map.util.FusedLocationSource;
import com.udangtangtang.haveibeen.databinding.ActivityMainBinding;
import com.udangtangtang.haveibeen.model.DBHelper;
import com.udangtangtang.haveibeen.util.PermissionHelper;
import com.udangtangtang.haveibeen.util.PictureScanHelper;

import android.widget.RatingBar;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, Overlay.OnClickListener {
    private ActivityMainBinding binding;
    private FusedLocationSource mLocationSource;
    private NaverMap mNaverMap;
    private UiSettings uiSettings;
    private InfoWindow mInfoWindow;
    private DBHelper dbHelper;
    private ArrayList<String> fileList;
    private Marker[] markers;
    private int dbSize;
    private String[] selectedLatLng;
    private PictureScanHelper pictureScanHelper;

    private static final String TAG="MainActivity";

    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final String[] PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    // 권한 요청에 대한 응답 코드
    private static final int REQ_PERMISSION_CALLBACK = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 외부 저장소 권한 요청
        PermissionHelper.checkPermission(this, READ_EXTERNAL_STORAGE, REQ_PERMISSION_CALLBACK);

        // 권한이 있다면 DB 초기화 및 사진 스캔
        // TODO : 이미지가 많을 경우 Thread로 구현?
        if (isPermissionGranted(this, READ_EXTERNAL_STORAGE)) {
            // DB 연결 및 초기화
            dbHelper = new DBHelper(this);
            dbHelper.isInitialDB();

            // 사진 스캔
            pictureScanHelper = new PictureScanHelper(this);
            fileList=new ArrayList<>();
            fileList = pictureScanHelper.scanPictures(this);

            // DB에 이미지 파일 추가하고 좌표 있는 이미지 개수 받아오기
            pictureScanHelper.initializePictureDB(dbHelper, fileList);
            dbSize = dbHelper.getSizeOfPictureDB();
        }

        // mapView 초기화
        binding.mapView.getMapAsync(this);
        mLocationSource = new FusedLocationSource(this, PERMISSION_REQUEST_CODE);
    }

    @UiThread
    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        // DB로부터 마커 추가
        int markerIdx = 0;
        Cursor cursor;
        markers = new Marker[dbSize];
        SQLiteDatabase sqlDB = dbHelper.getReadableDatabase();
        cursor = sqlDB.rawQuery("select distinct latitude, longtitude from myDB", null);
        while (cursor!=null&&cursor.moveToNext()) {
            markers[markerIdx] = new Marker();
            markers[markerIdx].setPosition(new LatLng(cursor.getDouble(0), cursor.getDouble(1)));
            markers[markerIdx].setMap(naverMap);

            // 마커 클릭 이벤트
            markers[markerIdx].setOnClickListener(this);
            markerIdx += 1;
        }

        // DB연결 종료
        sqlDB.close();

        // 마커 정보창 생성
        mInfoWindow = new InfoWindow();

        // 정보창 어댑터 설정
        mInfoWindow.setAdapter(new InfoWindow.DefaultViewAdapter(this) {
            @NonNull
            @Override
            protected View getContentView(@NonNull InfoWindow infoWindow) {
                // 선택한 정보창에 해당하는 마커 객체 가져오기
                Marker marker = infoWindow.getMarker();

                // 위/경도가 일치하는 기록 검색
                SQLiteDatabase sqlDB = dbHelper.getReadableDatabase();
                String[] params = {String.valueOf(marker.getPosition().latitude), String.valueOf(marker.getPosition().longitude)};
                Cursor cursor = sqlDB.rawQuery("select locName, address, date, comment, rating from myDB where latitude=? AND longtitude=?;", params);

                // 가져온 데이터로 뷰 만들기
                View view = View.inflate(MainActivity.this, R.layout.marker_infowindow, null);

                    if (cursor != null && cursor.moveToFirst()) {
                        ((TextView) view.findViewById(R.id.infoWindow_locationTitle)).setText(cursor.getString(0)==null ? "눌러서 작성" : cursor.getString(0));
                        ((TextView) view.findViewById(R.id.infoWindow_locationAddress)).setText(cursor.getString(1).equals("") ? " " : cursor.getString(1));
                        ((TextView) view.findViewById(R.id.infoWindow_datetime)).setText(cursor.getString(2)==null ? " " : cursor.getString(2));
                        ((TextView) view.findViewById(R.id.infoWindow_comment)).setText(cursor.getString(3)==null ? " " : cursor.getString(3));
                        ((RatingBar) view.findViewById(R.id.infoWindow_ratingBar)).setRating(String.valueOf(cursor.getFloat(4))==null ? (float) 0.0 : cursor.getFloat(4));
                    // 인텐트로 상세조회 페이지에 넘겨주기 위한 위/경도 기록
                    selectedLatLng= new String[]{String.valueOf(marker.getPosition().latitude), String.valueOf(marker.getPosition().longitude)};
                }

                // DB연결 종료
                sqlDB.close();

                return view;
            }
        });

        // 정보창 클릭 이벤트
        mInfoWindow.setOnClickListener(new Overlay.OnClickListener() {
            @Override
            public boolean onClick(@NonNull Overlay overlay) {
                // 정보창 클릭 시 정보 상세정보 확인 액티비티 전환
                Intent intent = new Intent(binding.getRoot().getContext(), RecordDetailActivity.class);
                // 인텐트에 위/경도를 첨부해서 전달
                intent.putExtra("selectedLatLng", selectedLatLng);
                startActivity(intent);
                return false;
            }
        });

        // 정보창이 아닌 지도 클릭 시 정보창 닫기
        naverMap.setOnMapClickListener((coord, point) -> {
            mInfoWindow.close();
        });

        // 지도 UI
        uiSettings = naverMap.getUiSettings();
        uiSettings.setCompassEnabled(true);
        uiSettings.setLocationButtonEnabled(true);

        // 위치 확인을 위한 locationSource와 권한 요청
        mNaverMap = naverMap;
        naverMap.setLocationSource(mLocationSource);
        ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_REQUEST_CODE);
    }

    // 지도 권한 요청 콜백 메소드
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mNaverMap.setLocationTrackingMode(LocationTrackingMode.Follow);
            }
        }
    }

    // 마커 클릭 이벤트
    @Override
    public boolean onClick(@NonNull Overlay overlay) {
        if (overlay instanceof Marker) {
            Marker marker = (Marker) overlay;
            if (marker.getInfoWindow() != null) {
                mInfoWindow.close();
            } else {
                mInfoWindow.open(marker);
            }
            return true;
        }
        return false;
    }

    // DB에 있는 좌표 정보들로 마커 생성
    public void addMarkersFromDB() {

    }

}