package com.udangtangtang.haveibeen.model

import androidx.room.ColumnInfo

data class LatLngTuple(
    @ColumnInfo val latitude : Double,
    @ColumnInfo val longtitude : Double

)
