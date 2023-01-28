package com.udangtangtang.haveibeen.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.udangtangtang.haveibeen.R
import com.udangtangtang.haveibeen.databinding.ActivitySplashBinding
import kotlinx.coroutines.*

class SplashActivity : AppCompatActivity() {
    // TODO : 한번 더 물어보기
    private lateinit var binding : ActivitySplashBinding
    private val TAG = "SplashActivity"

    companion object {
        private const val PERMISSION_REQUEST_CODE = 100
        private val PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.READ_MEDIA_IMAGES,
            android.Manifest.permission.ACCESS_MEDIA_LOCATION
        )

        private val requests= arrayListOf<String>()
        // 권한 요청에 대한 응답 코드
        private const val REQ_PERMISSION_CALLBACK = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if(checkPermissions()){
            Log.d(TAG, "Permission Granted")
            startActivity(Intent(this, MainActivity::class.java))
            this.finish()
        }else{
            requestPermissions()
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                if ((grantResults.isNotEmpty() &&
                            grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    Log.d(TAG, "Permission Granted")
                    startActivity(Intent(this, MainActivity::class.java))
                    this.finish()
                } else {
                    Toast.makeText(this, getString(R.string.text_failed_to_grant_permissions), Toast.LENGTH_LONG).show()
                    this.finish()
                }
                return
            }

            // Add other 'when' lines to check for other
            // permissions this app might request.
            else -> {
                // Ignore all other requests.
            }
        }
    }


    fun checkPermissions():Boolean {
        for (i in PERMISSIONS){
            if (ContextCompat.checkSelfPermission(this, i)!= PackageManager.PERMISSION_GRANTED){
                requests.add(i)
            }
        }

        if (requests.isNotEmpty()){
            return false
        }
        return true
    }

    fun requestPermissions(){
        if(!checkPermissions()){
            ActivityCompat.requestPermissions(this@SplashActivity,
                PERMISSIONS,
                PERMISSION_REQUEST_CODE
            )
        }
    }

}