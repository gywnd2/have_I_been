package com.udangtangtang.haveibeen.util

import android.annotation.SuppressLint
import android.content.*
import android.icu.text.SimpleDateFormat
import android.os.Build
import android.net.Uri
import android.os.AsyncTask
import android.provider.MediaStore
import android.provider.MediaStore.Images.Media
import android.text.TextUtils
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.exifinterface.media.ExifInterface
import com.udangtangtang.haveibeen.database.PictureDatabase
import com.udangtangtang.haveibeen.entity.PictureEntity
import java.io.IOException
import java.lang.NullPointerException

class PictureScanHelper(private val context: Context) {
    // TODO : 미디어 저장소 업데이트 감지, 동영상 스캔 추가
    // https://developer.android.com/training/data-storage/shared/media?hl=ko#detect-updates-media-files
    private val TAG = "pictureManager"
    private lateinit var geocodingHelper: GeocodingHelper
    private lateinit var pictureDB: PictureDatabase


    fun scanPictures() {
        val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.MediaColumns._ID,
                                MediaStore.MediaColumns.DATA,
                                MediaStore.MediaColumns.DISPLAY_NAME,
                                MediaStore.MediaColumns.DATE_TAKEN)
        val selectionArgs = arrayOf(
            dateToTimestamp(day = 1, month = 1, year = 1970).toString())
        val cursor = context.contentResolver.query(
            uri,
            projection,
            null,
            null,
            MediaStore.MediaColumns.DATE_TAKEN + " desc"
        )?.use{ cursor->
            var pictureList = mutableListOf<PictureEntity>()
            while (cursor.moveToNext()) {
                val idColumnIndex=cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
                val absolutePathOfImage =
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA))
                val nameOfFile =
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME))
                if (!TextUtils.isEmpty(absolutePathOfImage)) {
                    // 위/경도, 일시, 주소 얻기
                    var latLong: DoubleArray? = null
                    val datetime: String?
                    var address: String? = null
                    var photoUri = Uri.withAppendedPath(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        cursor.getString(idColumnIndex)
                    )
                    photoUri = MediaStore.setRequireOriginal(photoUri)
                    context.contentResolver.openInputStream(photoUri)?.use { stream ->
                        val exifInterface=ExifInterface(stream).run {
                            latLong = this.latLong
                            datetime = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_TAKEN))
                        }
                    }

                    // 좌표 정보를 통해 주소를 얻고 이를 파일명, 위/경도 정보, 촬영 일시 등을 모두 DB에 추가
                    geocodingHelper = GeocodingHelper(context)
                    if (latLong?.isNotEmpty() == true) {
                        address = geocodingHelper!!.getAddress(latLong!!.get(0), latLong!!.get(1))
                        Log.i(TAG, "Image " + absolutePathOfImage + address)
                    } else {
                        // 좌표 정보가 없을 경우
                        Log.i(
                            TAG,
                            "Image " + absolutePathOfImage + " has no coordination info."
                        )
                    }

                    if (latLong?.isNotEmpty() == true) {
                        val picture = PictureEntity(
                            absolutePathOfImage,
                            address!!,
                            null,
                            null,
                            latLong!!.get(0),
                            latLong!!.get(1)
                        )
                        pictureList.add(picture)
                    }


                    // TODO : 이미 추가된 사진인지 확인
                    /*
                val params = arrayOf(absolutePathOfImage)
                val cursor1 = sqlDB.rawQuery("select filen.me from myDB where filename=?;", params)
                if (cursor1 != null && cursor1.moveToFirst()) {
                    // 존재하면 pass
                } else {
                    // 존재하지 않으면 추가
                    fileList.add(absolutePathOfImage)
                }
                */

                    addPicture(pictureList)
                }
            }
        }
    }

    private fun dateToTimestamp(day: Int, month: Int, year: Int): Long =
        SimpleDateFormat("yyyy.MM.dd").let { formatter ->
            formatter.parse("$year.$month.$day")?.time ?: 0
        }

    ////////////////////////////////////////////////////////////////////////////////////////////////
//    fun initializePictureDB(dbHelper: DBHelper, fileList: ArrayList<String>) {
//        var latLong: DoubleArray? = DoubleArray(2)
//        var newPictureCount = 0
//        var noLatLngCount = 0
//        var address: String? = ""
//        var datetime: String? = ""

//        }
//
//        // 탐색 결과 출력
//        if (newPictureCount != 0) {
//            Toast.makeText(context, "새로운 사진 " + newPictureCount + "장을 찾았습니다.", Toast.LENGTH_LONG)
//                .show()
//        }
//        if (noLatLngCount != 0) {
//            Toast.makeText(
//                context, """
//     좌표 정보가 없는 사진 ${noLatLngCount}장을 찾았습니다.
//     지도에는 추가되지 않습니다.
//     """.trimIndent(), Toast.LENGTH_LONG
//            ).show()
//        }
//        if (newPictureCount == 0 && noLatLngCount == 0) {
//            Toast.makeText(context, "새로운 사진이 없습니다.", Toast.LENGTH_LONG).show()
//        }
//    }

    fun addPicture(pictureList : List<PictureEntity>){
        val insertTask= @SuppressLint("StaticFieldLeak")
        object: AsyncTask<Unit, Unit, Unit>(){
            override fun doInBackground(vararg params: Unit?) {
                for (picture in pictureList){
                    pictureDB.getPictureDao().insert(picture)
                }
            }

            override fun onPostExecute(result: Unit?) {
                super.onPostExecute(result)
                // TODO : DB에 사진 추가한 후 액션
            }
        }
    }
}