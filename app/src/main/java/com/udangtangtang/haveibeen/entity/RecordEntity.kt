package com.udangtangtang.haveibeen.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName="recordDB")
data class RecordEntity(
    @PrimaryKey val locationName: String,
    @ColumnInfo val address : String,
    @ColumnInfo val datetime : String,
    @ColumnInfo var comment : String?,
    @ColumnInfo var rating : Float?
)
