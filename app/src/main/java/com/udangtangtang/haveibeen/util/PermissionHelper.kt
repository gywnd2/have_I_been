package com.udangtangtang.haveibeen.util

import android.content.*
import android.os.Build
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.core.app.ActivityCompat
import com.udangtangtang.haveibeen.MainActivity

class PermissionHelper {
    // 권한 확인
    fun checkPermissionGranted(context: Context?, permissionName: String?): Boolean {
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
        var isPermissionGranted = checkPermissionGranted(context, permissionName)

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