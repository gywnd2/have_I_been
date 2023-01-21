package com.udangtangtang.haveibeen.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName="pictureDB", primaryKeys = ["latitude", "longtitude"])
data class PictureEntity(
    @ColumnInfo val latitude: Double,
    @ColumnInfo val longtitude: Double,
    @ColumnInfo val fileName: String,
    @ColumnInfo val address: String,
    @ColumnInfo val datetime : String,
    @ColumnInfo var rating: Float?,
    @ColumnInfo var comment: String?,
)
