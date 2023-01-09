package com.udangtangtang.haveibeen.model

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface PictureDao {
    @Insert
    fun insert(vararg pictureData : PictureData)
    @Delete
    fun delete(pictureData: PictureData)
    @Query("SELECT * FROM picturedata")
    fun getAll(): List<PictureData>
}