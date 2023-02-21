package com.udangtangtang.haveibeen.entity

import android.location.Address
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName="recordDB", primaryKeys = ["latitude", "longtitude"])
data class RecordEntity(
    @ColumnInfo val latitude : Double,
    @ColumnInfo val longtitude : Double,
    @ColumnInfo var locationName: String?,
    @ColumnInfo val address : String?,
    @ColumnInfo val datetime : String,
    @ColumnInfo var comment : String?,
    @ColumnInfo var rating : Float?
)
