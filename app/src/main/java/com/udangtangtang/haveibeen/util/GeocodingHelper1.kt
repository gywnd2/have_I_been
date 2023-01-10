package com.udangtangtang.haveibeen.util

import android.content.*
import android.database.sqlite.SQLiteOpenHelper
import android.database.sqlite.SQLiteDatabase
import com.udangtangtang.haveibeen.model.DBHelper
import android.location.Geocoder
import android.widget.Toast
import android.os.Build
import android.content.pm.PackageManager
import android.location.Address
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
import java.io.IOException

class GeocodingHelper(private val context: Context, latitude: Double, longtitude: Double) {
    private val geocoder: Geocoder
    private var addressList: List<Address>?

    // AdminArea -> 특별, 광역시/도
    // Locality -> 시
    // SubLocality -> 구 (특별 / 광역시의 구 포함)
    // Thoroughfare -> 읍/면/동/로
    private val address: Array<String?>

    init {

        // 역 지오코딩
        geocoder = Geocoder(context)
        address = arrayOfNulls(4)
        addressList = null
        try {
            // Geocoder를 통해 주소 획득
            addressList = geocoder.getFromLocation(latitude, longtitude, 10)
        } catch (e: IOException) {
        }
        if (addressList != null) {
            if (addressList.size == 0) {
                Toast.makeText(context, "해당 지역의 주소를 제공할 수 없습니다.", Toast.LENGTH_LONG).show()
            } else {
                address[0] = addressList.get(0).adminArea
                address[1] = addressList.get(0).locality
                address[2] = addressList.get(0).subLocality
                address[3] = addressList.get(0).thoroughfare
            }
        }
    }

    fun getAddress(): String? {
        var result: String? = ""
        for (i in 0..3) {
            // null을 건너 뛰고 각 단위를 이어 붙임
            // ex) 경기도 광명시 null 일직동 => 경기도 광명시 일직동
            //     경기도 안양시 만안구 석수2동 과 달리 구 단위가 없기 때문
            if (address[i] != null) {
                result += address[i]
                if (i != 3) {
                    result += " "
                }
            }
        }
        return result
    }
}