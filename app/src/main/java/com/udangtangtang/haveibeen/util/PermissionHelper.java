package com.udangtangtang.haveibeen.util;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.udangtangtang.haveibeen.MainActivity;

public class PermissionHelper {
    ////////////////////////////////////////////////////////////////////////////////////////////////
    /* 권한 확인 및 요청
    /  https://3001ssw.tistory.com/191
    */
    // 권한 확인
    public static boolean isPermissionGranted(Context context, String permissionName){
        boolean isPermissionGranted=true;

        // Android 6.0 이상
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            // 외부 저장소 권한이 있는지 확인
            if(PackageManager.PERMISSION_GRANTED!= ContextCompat.checkSelfPermission(context, permissionName)){
                isPermissionGranted=false;
            }else{
                isPermissionGranted=true;
            }
        }
        return isPermissionGranted;
    }

    public static boolean checkPermission(Context context, String permissionName, int iCallback){
        // 권한이 있는지 체크
        boolean isPermissionGranted=isPermissionGranted(context, permissionName);

        // 없으면 요청
        if(!isPermissionGranted){
            // 이전에 요청한 적 있는 권한이면 true
            boolean isRequestedBefore= ActivityCompat.shouldShowRequestPermissionRationale((MainActivity)context, permissionName);
            if(isRequestedBefore){
                // 처음 권한을 거부했을 때 다시 요청
                ActivityCompat.requestPermissions((MainActivity)context, new String[]{permissionName}, iCallback);
            }else{
                // 최초로 요청한다면
                ActivityCompat.requestPermissions((MainActivity)context, new String[]{permissionName}, iCallback);
                isPermissionGranted=true;
            }
        }
        return isPermissionGranted;
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////
}
