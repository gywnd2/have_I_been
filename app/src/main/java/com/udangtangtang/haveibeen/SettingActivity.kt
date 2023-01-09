package com.udangtangtang.haveibeen

import android.app.AlertDialog
import android.database.sqlite.SQLiteOpenHelper
import android.database.sqlite.SQLiteDatabase
import com.udangtangtang.haveibeen.model.DBHelper
import android.content.ContentValues
import com.udangtangtang.haveibeen.model.InfoWindowData
import com.udangtangtang.haveibeen.model.RecordData
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
import android.content.Intent
import com.udangtangtang.haveibeen.util.ViewPagerAdapter
import androidx.viewpager2.widget.ViewPager2
import android.content.DialogInterface
import com.udangtangtang.haveibeen.util.PictureScanHelper
import android.content.ComponentName
import com.udangtangtang.haveibeen.databinding.ActivitySettingBinding

class SettingActivity : AppCompatActivity() {
    private var binding: ActivitySettingBinding? = null
    private var dbHelper: DBHelper? = null
    private var pictureScanHelper: PictureScanHelper? = null
    private var builder: AlertDialog.Builder? = null
    private var dialog: AlertDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingBinding.inflate(
            layoutInflater
        )
        setContentView(binding!!.root)

        // Title 설정
        setTitle(R.string.setting_title)
        pictureScanHelper = PictureScanHelper(this)
        dbHelper = DBHelper(applicationContext)
        binding!!.settingButtonInitDb.setOnClickListener { // 확인 창
            builder = AlertDialog.Builder(this@SettingActivity)
            builder!!.setTitle(R.string.setting_alert_title_db_init)
                .setMessage(R.string.setting_alert_message_db_init)
                .setPositiveButton(R.string.yes) { dialog, which -> // DB 초기화
                    dbHelper!!.initializeDB()
                    restartApp()
                }
                .setNegativeButton(R.string.no) { dialog, which ->
                    // 아무 작업도 하지 않음
                }
            // 확인 창 표시
            dialog = builder!!.create()
            builder!!.show()
        }
        binding!!.settingButtonRescan.setOnClickListener { // 확인 창
            builder = AlertDialog.Builder(this@SettingActivity)
            builder!!.setTitle(R.string.setting_alert_title_rescan)
                .setMessage(R.string.setting_alert_message_rescan)
                .setPositiveButton(R.string.yes) { dialog, which -> // 사진 탐색
                    pictureScanHelper!!.scanPictures(applicationContext)
                    restartApp()
                }
                .setNegativeButton(R.string.no) { dialog, which ->
                    // 아무 작업도 하지 않음
                }
            // 확인 창 표시
            dialog = builder!!.create()
            builder!!.show()
        }
    }

    // 앱 재시작
    private fun restartApp() {
        val packageManager = packageManager
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        val componentName = intent!!.component
        val mIntent = Intent.makeRestartActivityTask(componentName)
        startActivity(mIntent)
        System.exit(0)
    }
}