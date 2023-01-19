package com.udangtangtang.haveibeen.dao

import androidx.room.Dao
import androidx.room.Query
import com.udangtangtang.haveibeen.entity.RecordEntity

@Dao
interface RecordDao {
    @Query("SELECT * FROM recordDB WHERE latitude LIKE :lat AND longtitude LIKE :lng")
    fun getRecord(lat : Double, lng : Double): RecordEntity
//    @Query("SELECT fileName FROM recordDB WHERE latitude LIKE :lat AND longtitude LIKE :lng")
//    fun getFileName
}