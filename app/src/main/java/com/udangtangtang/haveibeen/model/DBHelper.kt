package com.udangtangtang.haveibeen.model

import android.content.*
import android.database.sqlite.SQLiteOpenHelper
import android.database.sqlite.SQLiteDatabase
import com.udangtangtang.haveibeen.model.DBHelper
import com.udangtangtang.haveibeen.model.InfoWindowData
import com.udangtangtang.haveibeen.model.RecordData
import android.location.Geocoder
import android.widget.Toast
import android.os.Build
import android.content.pm.PackageManager
import android.database.Cursor
import androidx.core.content.ContextCompat
import com.udangtangtang.haveibeen.util.PermissionHelper
import androidx.core.app.ActivityCompat
import com.udangtangtang.haveibeen.MainActivity
import androidx.recyclerview.widget.RecyclerView
import com.udangtangtang.haveibeen.util.ViewPagerAdapter.ViewHolderPage
import android.view.ViewGroup
import android.view.LayoutInflater
import com.udangtangtang.haveibeen.R
import com.bumptech.glide.Glide
import com.udangtangtang.haveibeen.util.GeocodingHelper
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.udangtangtang.haveibeen.util.ViewPagerAdapter
import androidx.viewpager2.widget.ViewPager2
import com.udangtangtang.haveibeen.util.PictureScanHelper
import java.util.ArrayList

class DBHelper(private val mContext: Context) : SQLiteOpenHelper(
    mContext, "commentDB", null, 1
) {
    val RECORD_TABLE_NAME = "myDB"
    val ADDRESS_TABLE_NAME = "addressDB"
    val FILE_NAME = "filename"
    val LOCATION_NAME = "locName"
    val LATITUDE = "latitude"
    val LONGTITUDE = "longtitude"
    val ADDRESS = "address"
    val DATE = "date"
    val RATING = "rating"
    val COMMENT = "comment"
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("create table myDB (filename varchar(255), locName varchar(255), latitude varchar(255), longtitude varchar(255), address varchar(255), date datetime, rating float, comment varchar(255));")
        db.execSQL("create table addressDB(address varchar(255), count int)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("drop table if exists myDB;")
        db.execSQL("drop table if exists addressDB;")
        onCreate(db)
    }

    // DB 초기화
    fun initializeDB() {
        val myHelper = DBHelper(mContext)
        val sqlDB = myHelper.writableDatabase
        myHelper.onUpgrade(sqlDB, 1, 2)
        sqlDB.close()
    }// DB 초기화// DB가 이미 존재 하므로 Pass// DB 초기화

    // addressDB 존재 확인
// DB가 이미 존재 하므로 Pass// myDB 존재 확인
    // DB가 존재하는 지 확인하고 없을 때만 DB 초기화
    val isInitialDB: Unit
        get() {
            val myHelper = DBHelper(mContext)
            val sqlDB = myHelper.readableDatabase

            // myDB 존재 확인
            var cursor = sqlDB.rawQuery(
                "select name from sqlite_master where type='table' AND name='myDB'",
                null
            )
            if (cursor != null && cursor.moveToFirst()) {
                // DB가 이미 존재 하므로 Pass
            } else {
                // DB 초기화
                initializeDB()
            }

            // addressDB 존재 확인
            cursor = sqlDB.rawQuery(
                "select name from sqlite_master where type='table' AND name='addressDB'",
                null
            )
            if (cursor != null && cursor.moveToFirst()) {
                // DB가 이미 존재 하므로 Pass
            } else {
                // DB 초기화
                initializeDB()
            }
            sqlDB.close()
        }

    // 기록 수정 시 업데이트
    fun updateDB(fileName: String?, locationName: String?, rating: Float, comment: String?) {
        val myHelper = DBHelper(mContext)
        val sqlDB = myHelper.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(LOCATION_NAME, locationName)
        contentValues.put(RATING, rating)
        contentValues.put(COMMENT, comment)
        sqlDB.update(RECORD_TABLE_NAME, contentValues, "$FILE_NAME=?", arrayOf(fileName))
        sqlDB.close()
    }

    // DB에 사진 파일명 추가
    fun insertImageToDB(
        fileName: String?,
        latitude: Double,
        longtitude: Double,
        address: String?,
        datetime: String?
    ) {
        val myHelper = DBHelper(mContext)
        val sqlDB = myHelper.writableDatabase

        // myDB에 이미지 정보 추가
        var contentValues = ContentValues()
        contentValues.put(FILE_NAME, fileName)
        contentValues.put(LATITUDE, latitude.toString())
        contentValues.put(LONGTITUDE, longtitude.toString())
        contentValues.put(ADDRESS, address)
        contentValues.put(DATE, datetime)
        sqlDB.insert(RECORD_TABLE_NAME, null, contentValues)

        // addressDB에 주소 추가
        contentValues = ContentValues()
        contentValues.put("address", address)
        contentValues.put("count", 0)
        sqlDB.insert(ADDRESS_TABLE_NAME, null, contentValues)
        sqlDB.close()
    }// filename 개수 count 하여 반환

    // 좌표가 있는 이미지 리스트 개수 반환
    val sizeOfPictureDB: Int
        get() {
            val cursor: Cursor
            var result = 0
            val myHelper = DBHelper(mContext)
            val sqlDB = myHelper.writableDatabase
            // filename 개수 count 하여 반환
            cursor = sqlDB.rawQuery("select count(filename) from myDB;", null)
            while (cursor.moveToNext()) {
                result = cursor.getInt(0)
            }
            sqlDB.close()
            return result
        }

    fun getSameLocationPictures(latLng: Array<String?>): ArrayList<String> {
        // 같은 위/경도의 이미지 목록을 내보낼 ArrayList 선언
        val sameLocationPictures = ArrayList<String>()

        // DB에서 입력받은 위/경도를 갖는 사진 파일들 조회
        val myHelper = DBHelper(mContext)
        val sqlDB = myHelper.writableDatabase
        val cursor: Cursor?
        val params = arrayOf(latLng[0], latLng[1])
        cursor =
            sqlDB.rawQuery("select filename from myDB where latitude=? AND longtitude=?;", params)
        if (cursor != null && cursor.moveToFirst()) {
            // 1회 실행 후 while에서 moveToNext()
            sameLocationPictures.add(cursor.getString(0))
            while (cursor.moveToNext()) {
                sameLocationPictures.add(cursor.getString(0))
            }
        }
        return sameLocationPictures
    }

    // 위/경도로 filename 조회
    fun getFileNameByLatLng(latLng: Array<String?>?): String? {
        // 받아온 위/경도로 기록 조회
        val dbHelper = DBHelper(mContext)
        val sqlDB = dbHelper.readableDatabase
        val cursor = sqlDB.rawQuery(
            "select filename, locName, rating, comment from myDB where latitude=? AND longtitude=?;",
            latLng
        )
        return if (cursor != null && cursor.moveToFirst()) {
            cursor.getString(0)
        } else {
            null
        }
    }

    // filename으로 정보창 데이터 받기
    fun getInfoWindowData(latLng: Array<String?>): InfoWindowData {
        val infoWindowData = InfoWindowData()

        // 받아온 fileName으로 기록 조회
        val dbHelper = DBHelper(mContext)
        val sqlDB = dbHelper.readableDatabase
        val columns =
            arrayOf(dbHelper.LOCATION_NAME, dbHelper.DATE, dbHelper.COMMENT, dbHelper.RATING)
        val params = arrayOf(latLng[0], latLng[1])
        val cursor = sqlDB.rawQuery(
            "select locName, address, date, comment, rating from myDB where latitude=? AND longtitude=?;",
            params
        )

        // 데이터 내용 표시
        if (cursor != null && cursor.moveToFirst()) {
            infoWindowData.locationName = cursor.getString(0)
            infoWindowData.address = cursor.getString(1)
            infoWindowData.datetime = cursor.getString(2)
            infoWindowData.comment = cursor.getString(3)
            infoWindowData.rating = cursor.getFloat(4)
        }
        return infoWindowData
    }

    // filename으로 locName, rating, comment 찾기
    fun getRecordData(filename: String?): RecordData {
        val recordData = RecordData()

        // 받아온 fileName으로 기록 조회
        val dbHelper = DBHelper(mContext)
        val sqlDB = dbHelper.readableDatabase
        val columns =
            arrayOf(dbHelper.FILE_NAME, dbHelper.LOCATION_NAME, dbHelper.RATING, dbHelper.COMMENT)
        val params = arrayOf(filename)
        val cursor = sqlDB.query(
            dbHelper.RECORD_TABLE_NAME,
            columns,
            dbHelper.FILE_NAME + "=?",
            params,
            null,
            null,
            null
        )

        // 데이터 내용 표시
        if (cursor != null && cursor.moveToFirst()) {
            recordData.fileName = cursor.getString(0)
            recordData.locationName = cursor.getString(1)
            recordData.rating = cursor.getFloat(2)
            recordData.comment = cursor.getString(3)
        }

        // DB연결 종료
        sqlDB.close()
        return recordData
    }// 모든 주소 조회

    // 모든 주소 정보 리턴
    val allAddressInDB: ArrayList<String>
        get() {
            val myHelper = DBHelper(mContext)
            val sqlDB = myHelper.writableDatabase
            val addressList = ArrayList<String>()
            val cursor: Cursor
            // 모든 주소 조회
            cursor = sqlDB.rawQuery("select * from addressDB;", null)
            while (cursor.moveToNext()) {
                addressList.add(cursor.getString(0))
            }
            return addressList
        }

    // 모든 데이터 출력
    fun showAllData() {
        val myHelper = DBHelper(mContext)
        val sqlDB = myHelper.writableDatabase
        val cursor: Cursor
        cursor = sqlDB.rawQuery("select * from myDB;", null)
        if (cursor.count == 0) {
            //show message
            return
        }
        val buffer = StringBuffer()
        while (cursor.moveToNext()) {
            buffer.append(
                """
    $FILE_NAME${cursor.getString(0)}
    
    """.trimIndent()
            )
            buffer.append(
                """
    $LOCATION_NAME${cursor.getString(1)}
    
    """.trimIndent()
            )
            buffer.append(
                """
    $LATITUDE${cursor.getString(2)}
    
    """.trimIndent()
            )
            buffer.append(
                """
    $LONGTITUDE${cursor.getString(3)}
    
    """.trimIndent()
            )
            buffer.append(
                """
    $ADDRESS${cursor.getString(4)}
    
    """.trimIndent()
            )
            buffer.append(
                """
    $DATE${cursor.getString(5)}
    
    """.trimIndent()
            )
            buffer.append(
                """
    $RATING${cursor.getString(6)}
    
    """.trimIndent()
            )
            buffer.append(
                """
    $COMMENT${cursor.getString(7)}
    
    
    """.trimIndent()
            )
        }
        // Show all data
        Log.i("test", buffer.toString())
    }
}