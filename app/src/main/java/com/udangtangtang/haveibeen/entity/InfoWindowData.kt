package com.udangtangtang.haveibeen.entity

import android.location.Address
import androidx.room.ColumnInfo

data class InfoWindowData (
    @ColumnInfo val address: Address?,
    @ColumnInfo val datetime : String,
    @ColumnInfo var locName : String?,
    @ColumnInfo var rating : Float?
)