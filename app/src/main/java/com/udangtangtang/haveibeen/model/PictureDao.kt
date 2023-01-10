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
    @Query("SELECT * FROM PictureEntity")
    fun getAllPictures(): List<PictureEntity>
}