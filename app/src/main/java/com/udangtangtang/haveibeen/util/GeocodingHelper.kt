package com.udangtangtang.haveibeen.util

import android.content.*
import android.location.Geocoder
import android.widget.Toast
import android.location.Address
import android.location.Geocoder.GeocodeListener
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide.init
import com.udangtangtang.haveibeen.util.PictureScanHelper
import kotlinx.coroutines.*
import java.io.IOException

class GeocodingHelper(private val context: Context) {
    // TODO : 주소 수정 기능
    private var geocoder: Geocoder
    private val TAG = "GeocodingHelper"
    private var result = ""

    // AdminArea -> 특별, 광역시/도
    // Locality -> 시
    // SubLocality -> 구 (특별 / 광역시의 구 포함)
    // Thoroughfare -> 읍/면/동/로
    init {
        // 역 지오코딩
        geocoder = Geocoder(context)
    }


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun getAddress(latitude: Double, longtitude: Double): String {
        // Geocoder를 통해 주소 획득
        geocoder.getFromLocation(latitude, longtitude, 10, object : Geocoder.GeocodeListener {
            override fun onGeocode(addressList: MutableList<Address>) {
                result=addressList.get(0).getAddressLine(0)
//                result = addressList.get(0).getAddressLine(0).substring(5)
//                Log.d(TAG, result)
//                var result=""
//                Log.d(TAG, "!!!!!!!addressList:"+addressList.toString())
//                if (addressList.isNotEmpty()) {
//                    var address= mutableListOf<String>()
//
//                    if (addressList.size == 0) {
//                        Toast.makeText(context, "해당 지역의 주소를 제공할 수 없습니다.", Toast.LENGTH_LONG).show()
//                    } else {
//                        address.add(0, addressList.get(0).adminArea)
//                        address.add(1, addressList.get(0).locality)
//                        address.add(2, addressList.get(0).subLocality)
//                        address.add(3, addressList.get(0).thoroughfare)
//                    }
//
//                    for (i in 0..3) {
//                        // null을 건너 뛰고 각 단위를 이어 붙임
//                        // ex) 경기도 광명시 null 일직동 => 경기도 광명시 일직동
//                        //     경기도 안양시 만안구 석수2동 과 달리 구 단위가 없기 때문
//                        if (address[i] != null) {
//                            result += address[i]
//                            if (i != 3) {
//                                result += " "
//                            }
//                        }
//                    }
//                    Log.d(TAG, "!!!!!!!"+result)
//                }
            }

            override fun onError(errorMessage: String?) {
                super.onError(errorMessage)
            }
        })

        Log.d(TAG, "return address : " + result)
        return result
    }
}