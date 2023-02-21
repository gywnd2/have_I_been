package com.udangtangtang.haveibeen.database

import android.content.Context
import android.icu.text.AlphabeticIndex.Record
import androidx.room.*
import com.udangtangtang.haveibeen.dao.RecordDao
import com.udangtangtang.haveibeen.entity.RecordEntity
import com.udangtangtang.haveibeen.util.AddressTypeConverter
import kotlinx.coroutines.CoroutineScope

@Database(entities=arrayOf(RecordEntity::class), version=1)
@TypeConverters(AddressTypeConverter::class)
abstract class RecordDatabase :RoomDatabase(){
    abstract fun getRecordDao(): RecordDao

    companion object{
        @Volatile
        var INSTANCE: RecordDatabase?=null

        suspend fun getInstance(context : Context, scope : CoroutineScope) : RecordDatabase?{
            return INSTANCE?: synchronized(this){
                INSTANCE=Room.databaseBuilder(context.applicationContext, RecordDatabase::class.java, "Records.db")
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE
            }
        }
    }


}