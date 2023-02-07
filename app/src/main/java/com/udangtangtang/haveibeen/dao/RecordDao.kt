package com.udangtangtang.haveibeen.dao

import android.icu.text.AlphabeticIndex.Record
import androidx.room.*
import androidx.room.OnConflictStrategy.Companion.REPLACE
import com.udangtangtang.haveibeen.entity.RecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecordDao {
    @Insert(onConflict=REPLACE)
    suspend fun insert(record : RecordEntity)
    @Query("DELETE FROM recordDB WHERE latitude =:lat AND longtitude =:lng")
    suspend fun delete(lat : Double, lng : Double)
    @Update
    suspend fun update(record:RecordEntity)
    @Query("SELECT * FROM recordDB WHERE latitude =:lat AND longtitude =:lng")
    suspend fun getEntity(lat : Double, lng : Double): RecordEntity
    @Query("SELECT EXISTS(SELECT * FROM recordDB WHERE latitude =:lat AND longtitude =:lng)")
    suspend fun isExist(lat : Double, lng : Double): Boolean
}