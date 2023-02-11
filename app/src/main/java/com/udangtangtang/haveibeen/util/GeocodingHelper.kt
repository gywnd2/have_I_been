package com.udangtangtang.haveibeen.util

import android.app.Application
import android.content.*
import android.icu.text.AlphabeticIndex.Record
import android.location.Geocoder
import android.widget.Toast
import android.location.Address
import android.location.Geocoder.GeocodeListener
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide.init
import com.udangtangtang.haveibeen.repository.RecordRepository
import com.udangtangtang.haveibeen.util.PictureScanHelper
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import java.io.IOException

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
class GeocodingHelper(private val context: Context, private val db : RecordRepository) {
    // TODO : 주소 수정 기능
    private var geocoder: Geocoder
    private val TAG = "GeocodingHelper"
    var result=""

    // AdminArea -> 특별, 광역시/도
    // Locality -> 시
    // SubLocality -> 구 (특별 / 광역시의 구 포함)
    // Thoroughfare -> 읍/면/동/로
    init {
        // 역 지오코딩
        geocoder = Geocoder(context)
    }

    companion object{
        fun getAddressToString(addr:Address):String{
            var address=""
            with(addr){
                address+=adminArea
                address+=" "
                address+=locality
                address+=" "
                address+=subLocality
                address+=" "
                address+=thoroughfare
            }
            return address
        }
    }


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun getAddress(latitude: Double, longtitude: Double) {
        // Geocoder를 통해 주소 획득
        geocoder.getFromLocation(
            latitude,
            longtitude,
            3,
            object : Geocoder.GeocodeListener {
                override fun onGeocode(addressList: MutableList<Address>) {
                    for (i in addressList){
                        Log.d(TAG, i.subLocality.toString())
                    }
                    CoroutineScope(Dispatchers.IO).launch {
                        db.updatePictureAddress(latitude, longtitude, addressList[0])
                    }
                }
                override fun onError(errorMessage: String?) {
                    super.onError(errorMessage)
                }
            })
        }


    }