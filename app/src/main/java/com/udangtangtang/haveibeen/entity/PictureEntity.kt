package com.udangtangtang.haveibeen.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName="pictureDB")
data class PictureEntity(
    @PrimaryKey val fileName: String,
    @ColumnInfo val locationName: String,
    @ColumnInfo var rating: Float?,
    @ColumnInfo var comment: String?,
    @ColumnInfo val latitude: Double,
    @ColumnInfo val longtitude: Double
)
