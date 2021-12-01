package com.udangtangtang.haveibeen.util;

import android.content.Context;
import android.database.Cursor;

import androidx.exifinterface.media.ExifInterface;

import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.udangtangtang.haveibeen.model.DBHelper;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class PictureScanHelper {
    private final String TAG = "pictureManager";
    private Context context;
    private ExifInterface exifInterface;
    private GeocodingHelper geocodingHelper;

    public PictureScanHelper(Context mContext) {
        context = mContext;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
        /*
        /  사진 파일 리스트
        /  https://3001ssw.tistory.com/195
        */
    public ArrayList<String> scanPictures(Context context) {
        ArrayList<String> fileList = new ArrayList<>();
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.MediaColumns.DATA, MediaStore.MediaColumns.DISPLAY_NAME};

        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, MediaStore.MediaColumns.DATE_ADDED + " desc");
        int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        int columnDisplayName = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME);
        int lastIndex;

        DBHelper dbHelper = new DBHelper(context);
        SQLiteDatabase sqlDB = dbHelper.getReadableDatabase();
        while (cursor.moveToNext()) {
            String absolutePathOfImage = cursor.getString(columnIndex);
            String nameOfFile = cursor.getString(columnDisplayName);
            lastIndex = absolutePathOfImage.lastIndexOf(nameOfFile);
            lastIndex = lastIndex >= 0 ? lastIndex : nameOfFile.length() - 1;

            if (!TextUtils.isEmpty(absolutePathOfImage)) {
                // 이미 추가된 사진인지 확인
                String[] params = new String[]{absolutePathOfImage};
                Cursor cursor1 = sqlDB.rawQuery("select filename from myDB where filename=?;", params);
                if (cursor1 != null && cursor1.moveToFirst()) {
                    // 존재하면 pass
                } else {
                    // 존재하지 않으면 추가
                    fileList.add(absolutePathOfImage);
                }
            }
        }
        sqlDB.close();

        return fileList;
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public void initializePictureDB(DBHelper dbHelper, ArrayList<String> fileList) {
        double[] latLong = new double[2];
        int newPictureCount = 0;
        int noLatLngCount = 0;
        String address = "";
        String datetime = "";
        for (int i = 0; i < fileList.size(); i++) {
            try {
                // 위/경도, 일시, 주소 얻기
                if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q){
                    Uri photoUri = MediaStore.setRequireOriginal(Uri.parse(fileList.get(i)));
                    InputStream stream = context.getContentResolver().openInputStream(photoUri);
                    if (stream != null) {
                        ExifInterface exifInterface = new ExifInterface(stream);
                        latLong = exifInterface.getLatLong();
                        datetime = exifInterface.getAttribute(android.media.ExifInterface.TAG_DATETIME);
                        stream.close();
                    } else {
                        latLong = new double[2];
                        datetime = exifInterface.getAttribute(android.media.ExifInterface.TAG_DATETIME);
                    }
                }else{
                    exifInterface = new ExifInterface(fileList.get(i));
                    latLong = exifInterface.getLatLong();
                    datetime = exifInterface.getAttribute(android.media.ExifInterface.TAG_DATETIME);
                }
            } catch (IOException e) {
                System.out.println(e.toString());
            }
            try {
                geocodingHelper = new GeocodingHelper(context, latLong[0], latLong[1]);
                address = geocodingHelper.getAddress();
                dbHelper.insertImageToDB(fileList.get(i), latLong[0], latLong[1], address, datetime);
                Log.i(TAG, "Image " + fileList.get(i) + address);
                newPictureCount += 1;
            } catch (NullPointerException e) {
                // 좌표 정보가 없을 경우
                Log.i(TAG, "Image " + fileList.get(i) + " has no coordination info.");
                noLatLngCount += 1;
            }
        }

        // 탐색 결과 출력
        if (newPictureCount != 0) {
            Toast.makeText(context, "새로운 사진 " + newPictureCount + "장을 찾았습니다.", Toast.LENGTH_LONG).show();
        }

        if (noLatLngCount != 0) {
            Toast.makeText(context, "좌표 정보가 없는 사진 " + noLatLngCount + "장을 찾았습니다. \n지도에는 추가되지 않습니다.", Toast.LENGTH_LONG).show();
        }

        if(newPictureCount==0&&noLatLngCount==0){
            Toast.makeText(context, "새로운 사진이 없습니다.", Toast.LENGTH_LONG).show();
        }
    }
}
