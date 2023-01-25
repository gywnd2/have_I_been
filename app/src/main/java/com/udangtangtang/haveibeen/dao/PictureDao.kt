package com.udangtangtang.haveibeen.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import com.udangtangtang.haveibeen.model.AddressRankTuple
import com.udangtangtang.haveibeen.model.LatLngTuple
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
    @Query("SELECT COUNT(fileName) FROM pictureDB WHERE latitude =:lat AND longtitude =:lng")
    fun getFileCountOnLocation(lat:Double, lng:Double) : Int
    @Query("SELECT absolutePath FROM pictureDB WHERE latitude =:lat AND longtitude =:lng")
    fun getFileOnLocation(lat:Double, lng:Double) : List<String>
    @Query("SELECT latitude, longtitude FROM pictureDB WHERE fileName =:absoluteFilePath")
    fun getPictureLatLng(absoluteFilePath : String) : LatLngTuple
    @Query("SELECT datetime FROM pictureDB WHERE latitude =:lat AND longtitude =:lng")
    fun getDatetime(lat : Double, lng : Double) : String
    @Query("SELECT address, COUNT(address) as count FROM pictureDB")
    fun getAddressCount() : List<AddressRankTuple>
    @Query("SELECT address FROM pictureDB WHERE latitude =:lat AND longtitude =:lng")
    fun getAddress(lat:Double, lng:Double):String
    @Query("SELECT EXISTS (SELECT * FROM pictureDB WHERE fileName =:name AND latitude =:lat AND longtitude =:lng)")
    fun isExist(lat: Double, lng: Double, name:String) : Boolean
    @Query("UPDATE pictureDB SET address =:addr WHERE latitude =:lat AND longtitude =:lng")
    fun updateAddress(lat:Double, lng: Double, addr : String)
}