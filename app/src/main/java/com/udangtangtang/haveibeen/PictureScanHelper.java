package com.udangtangtang.haveibeen;

import android.content.Context;
import android.database.Cursor;

import androidx.exifinterface.media.ExifInterface;

import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
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
        while (cursor.moveToNext()) {
            String absolutePathOfImage = cursor.getString(columnIndex);
            String nameOfFile = cursor.getString(columnDisplayName);
            lastIndex = absolutePathOfImage.lastIndexOf(nameOfFile);
            lastIndex = lastIndex >= 0 ? lastIndex : nameOfFile.length() - 1;

            if (!TextUtils.isEmpty(absolutePathOfImage)) {
                fileList.add(absolutePathOfImage);
            }
        }
        return fileList;
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public void initializePictureDB(DBHelper dbHelper, ArrayList<String> fileList) {
        double[] latLong = new double[2];
        String address="";
        String datetime="";
        // TODO : 위/경도 데이터 없을 시 오류 해결하기
        for (int i = 0; i < fileList.size(); i++) {
            try {
                // 위/경도, 일시, 주소 얻기
                exifInterface = new ExifInterface(fileList.get(i));
                latLong = exifInterface.getLatLong();
                datetime=exifInterface.getAttribute(android.media.ExifInterface.TAG_DATETIME);
            } catch (IOException e) {
                System.out.println(e.toString());
            }
            try{
                geocodingHelper = new GeocodingHelper(context, latLong[0], latLong[1]);
                address=geocodingHelper.getAddress();
            }catch (NullPointerException e){
                System.out.println(e.toString());
            }
            try {
                dbHelper.insertImageToDB(fileList.get(i), latLong[0], latLong[1], address, datetime);
            } catch (NullPointerException e) {
                Log.i(TAG, "Image " + fileList.get(i) + " has no coordination info.");
            }
        }
    }
}
