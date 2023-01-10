package com.udangtangtang.haveibeen.util

import android.content.*
import android.database.sqlite.SQLiteOpenHelper
import android.database.sqlite.SQLiteDatabase
import com.udangtangtang.haveibeen.model.DBHelper
import android.location.Geocoder
import android.widget.Toast
import android.os.Build
import android.content.pm.PackageManager
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
import com.udangtangtang.haveibeen.util.ViewPagerAdapter
import androidx.viewpager2.widget.ViewPager2
import com.udangtangtang.haveibeen.util.PictureScanHelper

object PermissionHelper {
    ////////////////////////////////////////////////////////////////////////////////////////////////
    /* 권한 확인 및 요청
    /  https://3001ssw.tistory.com/191
    */
    // 권한 확인
    fun isPermissionGranted(context: Context?, permissionName: String?): Boolean {
        var isPermissionGranted = true

        // Android 6.0 이상
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 외부 저장소 권한이 있는지 확인
            isPermissionGranted =
                if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(
                        context!!,
                        permissionName!!
                    )
                ) {
                    false
                } else {
                    true
                }
        }
        return isPermissionGranted
    }

    fun checkPermission(context: Context?, permissionName: String?, iCallback: Int): Boolean {
        // 권한이 있는지 체크
        var isPermissionGranted = isPermissionGranted(context, permissionName)

        // 없으면 요청
        if (!isPermissionGranted) {
            // 이전에 요청한 적 있는 권한이면 true
            val isRequestedBefore = ActivityCompat.shouldShowRequestPermissionRationale(
                (context as MainActivity?)!!, permissionName!!
            )
            if (isRequestedBefore) {
                // 처음 권한을 거부했을 때 다시 요청
                ActivityCompat.requestPermissions(
                    context!!,
                    arrayOf<String?>(permissionName),
                    iCallback
                )
            } else {
                // 최초로 요청한다면
                ActivityCompat.requestPermissions(
                    context!!,
                    arrayOf<String?>(permissionName),
                    iCallback
                )
                isPermissionGranted = true
            }
        }
        return isPermissionGranted
    } ////////////////////////////////////////////////////////////////////////////////////////////////
}