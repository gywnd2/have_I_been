package com.udangtangtang.haveibeen.util

import android.location.Address
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class AddressTypeConverter {
    @TypeConverter
    fun toString(addr : Address?) : String{
        val type=object:TypeToken<Address>(){}.type
        return Gson().toJson(addr, type)
    }

    @TypeConverter
    fun toAddress(addrStr : String) : Address?{
        val type=object:TypeToken<Address>(){}.type
        return Gson().fromJson(addrStr, type)
    }
}