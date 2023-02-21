package com.udangtangtang.haveibeen.dao

import android.icu.text.AlphabeticIndex.Record
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.*
import androidx.room.OnConflictStrategy.Companion.REPLACE
import com.udangtangtang.haveibeen.entity.InfoWindowData
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
    fun getEntity(lat : Double, lng : Double): LiveData<RecordEntity>
    @Query("SELECT EXISTS(SELECT * FROM recordDB WHERE latitude =:lat AND longtitude =:lng)")
    suspend fun isExist(lat : Double, lng : Double): Boolean
    @Query("SELECT address, rating, datetime, locationName FROM recordDB WHERE latitude=:lat AND longtitude =:lng")
    fun getInfoWindowData(lat:Double, lng: Double):LiveData<InfoWindowData>
}