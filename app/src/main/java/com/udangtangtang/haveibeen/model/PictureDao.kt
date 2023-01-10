package com.udangtangtang.haveibeen.model

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query

@Dao
interface PictureDao {
    @Insert(onConflict = REPLACE)
    fun insert(picture : PictureEntity)
    @Delete
    fun delete(pictureEntity: PictureEntity)
    @Query("SELECT fileName FROM pictureentity")
    fun getFileName(): String
    @Query("SELECT COUNT(*) FROM pictureentity")
    fun getPictureNumbers() : Int
}