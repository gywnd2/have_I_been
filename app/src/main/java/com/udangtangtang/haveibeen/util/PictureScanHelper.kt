package com.udangtangtang.haveibeen.util

import android.app.Application
import android.content.*
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.exifinterface.media.ExifInterface
import com.udangtangtang.haveibeen.activity.MainActivity
import com.udangtangtang.haveibeen.entity.PictureEntity
import com.udangtangtang.haveibeen.repository.RecordRepository
import kotlinx.coroutines.*
import okhttp3.Dispatcher
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
class PictureScanHelper(private val context: Context, private val db:RecordRepository) {
    // TODO : 미디어 저장소 업데이트 감지, 동영상 스캔 추가, AsyncTask->Coroutine, 이전 버전?
    // https://developer.android.com/training/data-storage/shared/media?hl=ko#detect-updates-media-files
    private val TAG = "PictureScanHelper"
    private var geocodingHelper: GeocodingHelper

    init{
        geocodingHelper= GeocodingHelper(context, db)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun scanPictures() {
        val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.MediaColumns._ID,
                                MediaStore.MediaColumns.DATA,
                                MediaStore.MediaColumns.DISPLAY_NAME,
                                MediaStore.MediaColumns.DATE_ADDED)
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
            Log.d(TAG, cursor.count.toString())
            while (cursor.moveToNext()) {
                val idColumnIndex=cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
                val absolutePathOfImage =
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA))
                val nameOfFile =
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME))
                var datetime=
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_ADDED))
                val pictureAmount=cursor.count.toFloat()
                if (!TextUtils.isEmpty(absolutePathOfImage)) {
                    // Update Progress
                    (context as MainActivity).updateProgress((cursor.position+1).toFloat()/pictureAmount)
                    Log.d(TAG, "curr : "+cursor.position.toString()+"/"+pictureAmount.toString())
                    // 위/경도, 일시, 주소 얻기
                    var latLong: DoubleArray? = null
                    datetime=DateTimeFormatter.ofPattern("yyyy년 M월 dd일 HH:mm").format(
                        LocalDateTime.ofInstant(Instant.ofEpochMilli(datetime.toLong() * 1000), ZoneOffset.UTC)
                    )
                    var photoUri = Uri.withAppendedPath(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        cursor.getString(idColumnIndex)
                    )
                    photoUri = MediaStore.setRequireOriginal(photoUri)
                    context.contentResolver.openInputStream(photoUri)?.use { stream ->
                        val exifInterface=ExifInterface(stream).run {latLong = this.latLong}
                    }

                    // 좌표 정보를 통해 주소를 얻고 이를 파일명, 위/경도 정보, 촬영 일시 등을 모두 DB에 추가
                    CoroutineScope(Dispatchers.IO).launch {
                        async {
                            if (latLong?.isNotEmpty() == true and !db.isExistPicture(
                                    latLong!![0],
                                    latLong!![1],
                                    nameOfFile
                                )
                            ) {
                                val picture = PictureEntity(
                                    latLong!![0],
                                    latLong!![1],
                                    absolutePathOfImage,
                                    nameOfFile,
                                    null,
                                    datetime
                                )
                                pictureList.add(picture)
                                db.addPicture(picture)
                            } else {
                                // 좌표 정보가 없을 경우
                                Log.d(
                                    TAG,
                                    "Image " + absolutePathOfImage + " has no coordination info."
                                )
                            }
                        }.await()
                        async {
                            geocodingHelper.getAddress(latLong!![0], latLong!![1])
                        }
                    }

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


}