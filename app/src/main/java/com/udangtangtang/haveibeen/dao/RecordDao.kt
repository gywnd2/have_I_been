package com.udangtangtang.haveibeen.dao

import android.icu.text.AlphabeticIndex.Record
import androidx.room.*
import androidx.room.OnConflictStrategy.Companion.REPLACE
import com.udangtangtang.haveibeen.entity.RecordEntity

@Dao
interface RecordDao {
    @Insert(onConflict=REPLACE)
    fun insert(record : RecordEntity)
    @Query("DELETE FROM recordDB WHERE latitude =:lat AND longtitude =:lng")
    fun delete(lat : Double, lng : Double)
    @Update
    fun update(record:RecordEntity)
    @Query("SELECT * FROM recordDB WHERE latitude =:lat AND longtitude =:lng")
    fun getEntity(lat : Double, lng : Double):RecordEntity
    @Query("SELECT EXISTS(SELECT * FROM recordDB WHERE latitude =:lat AND longtitude =:lng)")
    fun isExist(lat : Double, lng : Double): Boolean
}