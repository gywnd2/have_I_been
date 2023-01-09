package com.udangtangtang.haveibeen.util

import android.content.*
import com.udangtangtang.haveibeen.model.DBHelper
import android.widget.Toast
import android.os.Build
import android.net.Uri
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import androidx.exifinterface.media.ExifInterface
import java.io.IOException
import java.lang.NullPointerException
import java.util.ArrayList

class PictureScanHelper(private val context: Context) {
    private val TAG = "pictureManager"
    private var exifInterface: ExifInterface? = null
    private var geocodingHelper: GeocodingHelper? = null

    ////////////////////////////////////////////////////////////////////////////////////////////////
    /*
        /  사진 파일 리스트
        /  https://3001ssw.tistory.com/195
        */
    fun scanPictures(context: Context): ArrayList<String> {
        val fileList = ArrayList<String>()
        val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.MediaColumns.DATA, MediaStore.MediaColumns.DISPLAY_NAME)
        val cursor = context.contentResolver.query(
            uri,
            projection,
            null,
            null,
            MediaStore.MediaColumns.DATE_ADDED + " desc"
        )
        val columnIndex = cursor!!.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
        val columnDisplayName = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
        var lastIndex: Int
        val dbHelper = DBHelper(context)
        val sqlDB = dbHelper.readableDatabase
        while (cursor.moveToNext()) {
            val absolutePathOfImage = cursor.getString(columnIndex)
            val nameOfFile = cursor.getString(columnDisplayName)
            lastIndex = absolutePathOfImage.lastIndexOf(nameOfFile)
            lastIndex = if (lastIndex >= 0) lastIndex else nameOfFile.length - 1
            if (!TextUtils.isEmpty(absolutePathOfImage)) {
                // 이미 추가된 사진인지 확인
                val params = arrayOf(absolutePathOfImage)
                val cursor1 = sqlDB.rawQuery("select filename from myDB where filename=?;", params)
                if (cursor1 != null && cursor1.moveToFirst()) {
                    // 존재하면 pass
                } else {
                    // 존재하지 않으면 추가
                    fileList.add(absolutePathOfImage)
                }
            }
        }
        sqlDB.close()
        return fileList
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    fun initializePictureDB(dbHelper: DBHelper, fileList: ArrayList<String>) {
        var latLong: DoubleArray? = DoubleArray(2)
        var newPictureCount = 0
        var noLatLngCount = 0
        var address: String? = ""
        var datetime: String? = ""
        for (i in fileList.indices) {
            try {
                // 위/경도, 일시, 주소 얻기
                // Android Q 이상의 경우 위치 정보가 사진에 직접적으로 담겨 있지 않음
                // Q이상 일 경우
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val photoUri = MediaStore.setRequireOriginal(Uri.parse(fileList[i]))
                    val stream = context.contentResolver.openInputStream(photoUri)
                    if (stream != null) {
                        val exifInterface = ExifInterface(stream)
                        latLong = exifInterface.latLong
                        datetime =
                            exifInterface.getAttribute(android.media.ExifInterface.TAG_DATETIME)
                        stream.close()
                    } else {
                        latLong = DoubleArray(2)
                        datetime =
                            exifInterface!!.getAttribute(android.media.ExifInterface.TAG_DATETIME)
                    }
                    // Q 이전 버전일 경우
                } else {
                    exifInterface = ExifInterface(fileList[i])
                    latLong = exifInterface!!.latLong
                    datetime =
                        exifInterface!!.getAttribute(android.media.ExifInterface.TAG_DATETIME)
                }
            } catch (e: IOException) {
                println(e.toString())
            }
            try {
                // 좌표 정보를 통해 주소를 얻고 이를 파일명, 위/경도 정보, 촬영 일시 등을 모두 DB에 추가
                geocodingHelper = GeocodingHelper(context, latLong!![0], latLong[1])
                address = geocodingHelper!!.address
                dbHelper.insertImageToDB(fileList[i], latLong[0], latLong[1], address, datetime)
                Log.i(TAG, "Image " + fileList[i] + address)
                newPictureCount += 1
            } catch (e: NullPointerException) {
                // 좌표 정보가 없을 경우
                Log.i(TAG, "Image " + fileList[i] + " has no coordination info.")
                // 좌표 정보 없는 사진 개수 count
                noLatLngCount += 1
            }
        }

        // 탐색 결과 출력
        if (newPictureCount != 0) {
            Toast.makeText(context, "새로운 사진 " + newPictureCount + "장을 찾았습니다.", Toast.LENGTH_LONG)
                .show()
        }
        if (noLatLngCount != 0) {
            Toast.makeText(
                context, """
     좌표 정보가 없는 사진 ${noLatLngCount}장을 찾았습니다. 
     지도에는 추가되지 않습니다.
     """.trimIndent(), Toast.LENGTH_LONG
            ).show()
        }
        if (newPictureCount == 0 && noLatLngCount == 0) {
            Toast.makeText(context, "새로운 사진이 없습니다.", Toast.LENGTH_LONG).show()
        }
    }
}