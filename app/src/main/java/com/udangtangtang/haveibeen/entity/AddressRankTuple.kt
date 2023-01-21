package com.udangtangtang.haveibeen.entity

import androidx.room.ColumnInfo

data class AddressRankTuple(
    @ColumnInfo val address: String,
    @ColumnInfo val count : Int
)
