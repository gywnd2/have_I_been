package com.udangtangtang.haveibeen.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class RecordWindow(
    @PrimaryKey val locationName: String,
    @ColumnInfo val address : String,
    @ColumnInfo val datetime : String,
    @ColumnInfo var comment : String?,
    @ColumnInfo var rating : Float?
)
