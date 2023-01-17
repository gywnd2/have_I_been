package com.udangtangtang.haveibeen.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import com.udangtangtang.haveibeen.entity.PictureEntity

@Dao
interface PictureDao {
    @Insert(onConflict = REPLACE)
    fun insert(picture : PictureEntity)
    @Delete
    fun delete(pictureEntity: PictureEntity)
//    @Query("SELECT fileName FROM pictureentity WHERE fileName LIKE")
//    fun getFileName(): String
    @Query("SELECT COUNT(*) FROM pictureDB")
    fun getPictureNumbers() : Int
    @Query("SELECT fileName FROM pictureDB")
    fun getFileList() : ArrayList<String>
    @Query("SELECT latitude, longtitude FROM pictureDB WHERE fileName LIKE :absoluteFilePath")
    fun getPictureLatLng(absoluteFilePath : String) : DoubleArray
}