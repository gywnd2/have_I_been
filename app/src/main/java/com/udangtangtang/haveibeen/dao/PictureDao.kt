package com.udangtangtang.haveibeen.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import com.udangtangtang.haveibeen.entity.AddressRankTuple
import com.udangtangtang.haveibeen.entity.LatLngTuple
import com.udangtangtang.haveibeen.entity.PictureEntity

@Dao
interface PictureDao {
    @Insert(onConflict = REPLACE)
    fun insert(picture : PictureEntity)
    @Delete
    fun delete(pictureEntity: PictureEntity)
    @Query("SELECT COUNT(*) FROM pictureDB")
    fun getPictureNumbers() : Int
    @Query("SELECT fileName FROM pictureDB")
    fun getFileList() : List<String>
    @Query("SELECT latitude, longtitude FROM pictureDB WHERE fileName =:absoluteFilePath")
    fun getPictureLatLng(absoluteFilePath : String) : LatLngTuple
    @Query("SELECT datetime FROM pictureDB WHERE latitude =:lat AND longtitude =:lng")
    fun getDatetime(lat : Double, lng : Double) : String
    @Query("SELECT address, COUNT(address) as count FROM pictureDB")
    fun getAddressCount() : List<AddressRankTuple>
    @Query("SELECT address FROM pictureDB WHERE latitude =:lat AND longtitude =:lng")
    fun getAddress(lat:Double, lng:Double):String
    @Query("SELECT EXISTS (SELECT * FROM pictureDB WHERE fileName =:name)")
    fun isExist(name : String) : Boolean
}