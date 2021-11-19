package com.udangtangtang.haveibeen;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.media.ExifInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.udangtangtang.haveibeen.databinding.ActivityRecordDetailBinding;
import com.udangtangtang.haveibeen.model.DBHelper;
import com.udangtangtang.haveibeen.model.RecordData;
import com.udangtangtang.haveibeen.util.GeocodingHelper;
import com.udangtangtang.haveibeen.util.ViewPagerAdapter;

import java.io.IOException;
import java.util.List;

public class RecordDetailActivity extends AppCompatActivity {
    private ActivityRecordDetailBinding binding;
    private DBHelper dbHelper;
    private String[] selectedLatLng;
    private ExifInterface exifInterface;
    private GeocodingHelper geocodingHelper;
    private List<Address> addressList;
    private String firstFileName;
    private AlertDialog.Builder builder;
    private AlertDialog dialog;
    private RecordData recordData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRecordDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // MainActivity로 부터 fileName 받아오기
        Intent intent = getIntent();
        selectedLatLng = new String[2];
        selectedLatLng = intent.getStringArrayExtra("selectedLatLng");

        // 전달받은 위/경도 정보를 ViewPager 어댑터로 전달
        binding.recordDetailViewpager2.setAdapter(new ViewPagerAdapter(this, selectedLatLng));
        // 가로 스크롤 설정
        binding.recordDetailViewpager2.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);

        // DB연결을 위해 Helper 초기화
        dbHelper = new DBHelper(this);

        // 같은 위/경도를 갖는 이미지 중 맨 처음 파일에 데이터 저장
        firstFileName = dbHelper.getFileNameByLatLng(selectedLatLng);

        // 데이터 가져오기
        recordData= dbHelper.getRecordData(firstFileName);

        // Indicator 설정
        binding.recordDetailImageIndicator.setViewPager(binding.recordDetailViewpager2);
        binding.recordDetailImageIndicator.createIndicators(dbHelper.getSameLocationPictures(selectedLatLng).size(), 0);

        // 시간, 날짜 정보를 가져오기 위한 exifInterface 초기화
        try {
            exifInterface = new ExifInterface(firstFileName);
        } catch (IOException e) {

        }
        // 지오코딩을 위한 Helper 초기화
        geocodingHelper = new GeocodingHelper(this, Double.valueOf(selectedLatLng[0]), Double.valueOf(selectedLatLng[1]));
        // 주소 표시
        binding.recordDetailAddress.setText(geocodingHelper.getAddress());
        binding.recordDetailLocationName.setText(recordData.getLocationName() == null ? getApplicationContext().getResources().getString(R.string.record_detail_no_locName) : recordData.getLocationName());
        binding.recordDetailDatetime.setText(exifInterface.getAttribute(ExifInterface.TAG_DATETIME));
        binding.recordDetailRating.setRating(recordData.getRating() == 0.0 ? (float) 0.0 : recordData.getRating());
        binding.recordDetailComment.setText(recordData.getComment() == null ? getApplicationContext().getResources().getString(R.string.record_detail_no_comment) : recordData.getComment());

        // DB연결 종료

        // 수정용 View 가리기
        binding.recordDetailEditLocationName.setVisibility(View.INVISIBLE);
        binding.recordDetailEditRating.setVisibility(View.INVISIBLE);
        binding.recordDetailEditComment.setVisibility(View.INVISIBLE);
        binding.recordDetailEditButtonSave.setVisibility(View.INVISIBLE);

        // 수정 클릭시 EditText로 모두 전환
        binding.recordDetailButtonEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 보기용 View는 가리기
                binding.recordDetailLocationName.setVisibility(View.INVISIBLE);
                binding.recordDetailRating.setVisibility(View.INVISIBLE);
                binding.recordDetailComment.setVisibility(View.INVISIBLE);
                binding.recordDetailButtonEdit.setVisibility(View.INVISIBLE);

                // 수정용 View와 보기용 View의 내용 일치시키기
                binding.recordDetailEditLocationName.setText(binding.recordDetailLocationName.getText());
                binding.recordDetailEditRating.setRating(binding.recordDetailRating.getRating());
                binding.recordDetailEditComment.setText(binding.recordDetailComment.getText());

                // 수정용 View 표시
                binding.recordDetailEditLocationName.setVisibility(View.VISIBLE);
                binding.recordDetailEditRating.setVisibility(View.VISIBLE);
                binding.recordDetailEditComment.setVisibility(View.VISIBLE);
                binding.recordDetailEditButtonSave.setVisibility(View.VISIBLE);
            }
        });

        // 저장 클릭시 TextView로 모두 전환
        binding.recordDetailEditButtonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 저장 확인 창
                builder = new AlertDialog.Builder(RecordDetailActivity.this);
                builder.setTitle(R.string.record_detail_title_save)
                        .setMessage(R.string.record_detail_message_save)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // DB 업데이트
                                dbHelper.updateDB(firstFileName, binding.recordDetailEditLocationName.getText().toString(), binding.recordDetailEditRating.getRating(), binding.recordDetailEditComment.getText().toString());

                                // 저장 되었음을 확인하기 위해 저장 시에는 다시 조회하여 출력
                                // 받아온 fileName으로 기록 조회
                                recordData=dbHelper.getRecordData(firstFileName);
                                // 데이터 내용 표시
                                    binding.recordDetailLocationName.setText(recordData.getLocationName() == null ? "" : recordData.getLocationName());
                                    binding.recordDetailAddress.setText(geocodingHelper.getAddress());
                                    binding.recordDetailDatetime.setText(exifInterface.getAttribute(ExifInterface.TAG_DATETIME));
                                    binding.recordDetailRating.setRating(recordData.getRating() == 0.0 ? (float) 0.0 : recordData.getRating());
                                    binding.recordDetailComment.setText(recordData.getComment() == null ? "" : recordData.getComment());

                                // 수정용 View 가리기
                                binding.recordDetailEditLocationName.setVisibility(View.INVISIBLE);
                                binding.recordDetailEditRating.setVisibility(View.INVISIBLE);
                                binding.recordDetailEditComment.setVisibility(View.INVISIBLE);
                                binding.recordDetailEditButtonSave.setVisibility(View.INVISIBLE);

                                // 보기용 View 표시
                                binding.recordDetailLocationName.setVisibility(View.VISIBLE);
                                binding.recordDetailRating.setVisibility(View.VISIBLE);
                                binding.recordDetailComment.setVisibility(View.VISIBLE);
                                binding.recordDetailButtonEdit.setVisibility(View.VISIBLE);

                                Toast.makeText(getApplicationContext(), "저장했습니다.", Toast.LENGTH_LONG).show();
                            }
                        })
                        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });

                // 확인창 표시
                dialog = builder.create();
                builder.show();
            }
        });
    }

}