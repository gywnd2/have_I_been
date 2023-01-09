package com.udangtangtang.haveibeen.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class PictureData(
    @PrimaryKey val fileName : String,
    @ColumnInfo val locationName : String,
    @ColumnInfo var rating : Float?,
    @ColumnInfo var comment : String?

)
