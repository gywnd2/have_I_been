package com.udangtangtang.haveibeen.util

import android.location.Address
import android.util.Log
import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@ProvidedTypeConverter
class AddressTypeConverter(private val gson: Gson) {
    @TypeConverter
    fun toJsonString(addr : Address?) : String?{
        Log.d("TypeConverter / received addr : ",addr.toString())
        val type=object:TypeToken<Address>(){}.type
        Log.d("TypeConverter / address to string", gson.toJson(addr, type))
        return gson.toJson(addr, type)
    }

    @TypeConverter
    fun fromJsonString(addrStr : String) : Address{
        val type=object:TypeToken<Address>(){}.type
//        Log.d("TypeConverter / string to address", gson.fromJson(addrStr, type).toString())
        return gson.fromJson(addrStr, type)
    }
}