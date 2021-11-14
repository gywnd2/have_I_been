package com.udangtangtang.haveibeen;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.ExifInterface;
import android.net.Uri;
import android.opengl.Visibility;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.udangtangtang.haveibeen.databinding.ActivityRecordDetailBinding;

import java.io.IOException;

public class RecordDetailActivity extends AppCompatActivity {
    private ActivityRecordDetailBinding binding;
    private DBHelper dbHelper;
    private String fileName;
    private ExifInterface exifInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRecordDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // DB조회를 위해 Helper 초기화
        dbHelper = new DBHelper(this);

        // MainActivity로 부터 fileName 받아오기
        Intent intent = getIntent();
        fileName = intent.getStringExtra("fileName");

        // 받아온 fileName으로 Exif 데이터 조회
        try {
            exifInterface = new ExifInterface(fileName);
            // 사진 표시
            binding.recordDetailImage.setImageURI(Uri.parse(fileName));
        } catch (IOException e) {
            Toast.makeText(this, "선택한 사진이 존재하지 않습니다.", Toast.LENGTH_LONG).show();
        }

        // 받아온 fileName으로 기록 조회
        SQLiteDatabase sqlDB = dbHelper.getReadableDatabase();
        String[] columns = {dbHelper.LOCATION_NAME, dbHelper.RATING, dbHelper.COMMENT};
        String[] params = {fileName};
        Cursor cursor = sqlDB.query(dbHelper.TABLE_NAME, columns, dbHelper.FILE_NAME + "=?", params, null, null, null);

        // 데이터 내용 표시
        if (cursor != null && cursor.moveToFirst()) {
            binding.recordDetailLocationName.setText(cursor.getString(0).equals("null") ? "'수정'을 터치하여 입력해보세요." : cursor.getString(0));
            binding.recordDetailAddress.setText();
            binding.recordDetailDatetime.setText(exifInterface.getAttribute(ExifInterface.TAG_DATETIME));
            binding.recordDetailRating.setRating(String.valueOf(cursor.getFloat(1)).equals("null") ? (float) 0.0 : cursor.getFloat(1));
            binding.recordDetailComment.setText(cursor.getString(2).equals("null") ? "어떤 장소였나요?" : cursor.getString(2));
        }

        // DB연결 종료
        sqlDB.close();

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
                // TODO : 저장하기 구현

                // 저장 되었음을 확인하기 위해 저장 시에는 다시 조회하여 출력
                // 받아온 fileName으로 기록 조회
                SQLiteDatabase sqlDB = dbHelper.getReadableDatabase();
                String[] columns = {dbHelper.LOCATION_NAME, dbHelper.RATING, dbHelper.COMMENT};
                String[] params = {fileName};
                Cursor cursor = sqlDB.query(dbHelper.TABLE_NAME, columns, dbHelper.FILE_NAME + "=?", params, null, null, null);

                // 데이터 내용 표시
                if (cursor != null && cursor.moveToFirst()) {
                    binding.recordDetailLocationName.setText(cursor.getString(0).equals("null") ? "'수정'을 터치하여 입력해보세요." : cursor.getString(0));
//                    binding.recordDetailAddress.setText(cursor.getString(0).equals("null") ? "주소 가져오기" : cursor.getString(0));
//                    binding.recordDetailDatetime.setText(cursor.getString(0).equals("null") ? "시간 가져오기" : cursor.getString(0));
                    binding.recordDetailRating.setRating(String.valueOf(cursor.getFloat(1)).equals("null") ? (float) 0.0 : cursor.getFloat(1));
                    binding.recordDetailComment.setText(cursor.getString(2).equals("null") ? "어떤 장소였나요?" : cursor.getString(2));
                }

                // DB연결 종료
                sqlDB.close();

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
            }
        });
    }
}