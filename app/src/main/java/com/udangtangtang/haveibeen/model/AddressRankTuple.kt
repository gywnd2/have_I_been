package com.udangtangtang.haveibeen.model

import androidx.room.ColumnInfo

data class AddressRankTuple(
    @ColumnInfo val address: String,
    @ColumnInfo val count : Int
)
