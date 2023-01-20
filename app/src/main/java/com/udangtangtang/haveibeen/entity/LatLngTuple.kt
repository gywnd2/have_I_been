package com.udangtangtang.haveibeen.entity

import androidx.room.ColumnInfo

data class LatLngTuple(
    @ColumnInfo val latitude : Double,
    @ColumnInfo val longtitude : Double

)
