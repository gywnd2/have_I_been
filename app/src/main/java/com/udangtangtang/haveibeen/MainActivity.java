package com.udangtangtang.haveibeen;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

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

import android.provider.MediaStore;
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
import com.udangtangtang.haveibeen.model.InfoWindowData;
import com.udangtangtang.haveibeen.util.PermissionHelper;
import com.udangtangtang.haveibeen.util.PictureScanHelper;

import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

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
    private InfoWindowData infoWindowData;
    private long backKeyTime=0;

    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final String[] PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    // ?????? ????????? ?????? ?????? ??????
    private static final int REQ_PERMISSION_CALLBACK = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setTitle(R.string.app_name);

        // ?????? ????????? ?????? ??????
        PermissionHelper.checkPermission(this, READ_EXTERNAL_STORAGE, REQ_PERMISSION_CALLBACK);

        // ????????? ????????? DB ????????? ??? ?????? ??????
        if (isPermissionGranted(this, READ_EXTERNAL_STORAGE)) {
            // DB ?????? ??? ?????????
            dbHelper = new DBHelper(this);
            dbHelper.isInitialDB();

            // ?????? ??????
            pictureScanHelper = new PictureScanHelper(this);
            fileList = new ArrayList<>();
            fileList = pictureScanHelper.scanPictures(this);

            // DB??? ????????? ?????? ???????????? ?????? ?????? ????????? ?????? ????????????
            pictureScanHelper.initializePictureDB(dbHelper, fileList);
            dbSize = dbHelper.getSizeOfPictureDB();
        }

        // mapView ?????????
        binding.mainMapView.getMapAsync(this);
        mLocationSource = new FusedLocationSource(this, PERMISSION_REQUEST_CODE);

        // Floating Button (Settings)
        binding.mainFabSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SettingActivity.class);
                startActivity(intent);
            }
        });

//         Floating Button (Ranking), ?????? ??? ?????? ???????????? ??????
        binding.mainFabRanking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getApplicationContext(), RankingActivity.class);
                startActivity(intent);
            }
        });
    }

    @UiThread
    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        // DB????????? ?????? ??????
        if (isPermissionGranted(this, READ_EXTERNAL_STORAGE)) {
            int markerIdx = 0;
            Cursor cursor;
            markers = new Marker[dbSize];
            SQLiteDatabase sqlDB = dbHelper.getReadableDatabase();
            cursor = sqlDB.rawQuery("select distinct latitude, longtitude from myDB", null);
            while (cursor != null && cursor.moveToNext()) {
                markers[markerIdx] = new Marker();
                markers[markerIdx].setPosition(new LatLng(cursor.getDouble(0), cursor.getDouble(1)));
                markers[markerIdx].setMap(naverMap);

                // ?????? ?????? ?????????
                markers[markerIdx].setOnClickListener(this);
                markerIdx += 1;
            }

            // DB?????? ??????
            sqlDB.close();
        }

        // ?????? ????????? ??????
        mInfoWindow = new InfoWindow();

        // ????????? ????????? ??????
        mInfoWindow.setAdapter(new InfoWindow.DefaultViewAdapter(this) {
            @NonNull
            @Override
            protected View getContentView(@NonNull InfoWindow infoWindow) {
                // ????????? ???????????? ???????????? ?????? ?????? ????????????
                Marker marker = infoWindow.getMarker();

                // ???????????? ???????????? ???????????? ???????????? ?????? ???/?????? ??????
                selectedLatLng = new String[]{String.valueOf(marker.getPosition().latitude), String.valueOf(marker.getPosition().longitude)};

                // ???/????????? ????????? ????????? ????????????
                infoWindowData = new InfoWindowData();
                infoWindowData = dbHelper.getInfoWindowData(selectedLatLng);

                // ????????? ???????????? ??? ?????????
                View view = View.inflate(MainActivity.this, R.layout.marker_infowindow, null);

                ((TextView) view.findViewById(R.id.infoWindow_locationTitle)).setText(infoWindowData.getLocationName() == null ? getString(R.string.record_detail_no_locName) : infoWindowData.getLocationName());
                ((TextView) view.findViewById(R.id.infoWindow_locationAddress)).setText(infoWindowData.getAddress()==null ? getString(R.string.no_location_info) : infoWindowData.getAddress());
                ((TextView) view.findViewById(R.id.infoWindow_datetime)).setText(infoWindowData.getDatetime() == null ? getString(R.string.no_datetime_info) : infoWindowData.getDatetime());
                ((TextView) view.findViewById(R.id.infoWindow_comment)).setText(infoWindowData.getComment() == null ? getString(R.string.record_detail_no_comment) : infoWindowData.getComment());
                ((RatingBar) view.findViewById(R.id.infoWindow_ratingBar)).setRating(infoWindowData.getRating() == 0.0 ? (float) 0.0 : infoWindowData.getRating());

                return view;
            }
        });

        // ????????? ?????? ?????????
        mInfoWindow.setOnClickListener(new Overlay.OnClickListener() {
            @Override
            public boolean onClick(@NonNull Overlay overlay) {
                // ????????? ?????? ??? ?????? ???????????? ?????? ???????????? ??????
                Intent intent = new Intent(binding.getRoot().getContext(), RecordDetailActivity.class);
                // ???????????? ???/????????? ???????????? ??????
                intent.putExtra("selectedLatLng", selectedLatLng);
                startActivity(intent);
                return false;
            }
        });

        // ???????????? ?????? ?????? ?????? ??? ????????? ??????
        naverMap.setOnMapClickListener((coord, point) -> {
            mInfoWindow.close();
        });

        // ?????? UI
        uiSettings = naverMap.getUiSettings();
        uiSettings.setCompassEnabled(true);
        uiSettings.setLocationButtonEnabled(true);

        // ?????? ????????? ?????? locationSource??? ?????? ??????
        mNaverMap = naverMap;
        naverMap.setLocationSource(mLocationSource);
        ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_REQUEST_CODE);
    }

    // ?????? ?????? ?????? ?????? ?????????
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

    // ?????? ?????? ?????????
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

    // ???????????? ?????????
    @Override
    public void onBackPressed() {
        if(System.currentTimeMillis()>backKeyTime+2000){
            backKeyTime=System.currentTimeMillis();
            Toast.makeText(this, getString(R.string.backkeypressed), Toast.LENGTH_LONG).show();
            return;
        }

        if(System.currentTimeMillis()<=backKeyTime+2000){
            this.finish();
        }
    }

}