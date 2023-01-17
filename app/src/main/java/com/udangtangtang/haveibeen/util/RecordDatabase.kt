package com.udangtangtang.haveibeen.util

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.udangtangtang.haveibeen.dao.RecordDao
import com.udangtangtang.haveibeen.entity.RecordEntity

@Database(entities=arrayOf(RecordEntity::class), version=1)
abstract class RecordDatabase :RoomDatabase(){
    abstract fun recordDao(): RecordDao

    companion object{
        var INSTANCE: RecordDatabase?=null

        fun getInstance(context : Context) : RecordDatabase?{
            if(INSTANCE==null){
                synchronized(RecordDatabase::class){
                    INSTANCE= Room.databaseBuilder(context.applicationContext, RecordDatabase::class.java, "Records.db")
                    // Drop database when update
                    // TODO : Migrate DB on NON-Destructive way
                        .fallbackToDestructiveMigration()
                        .build()
                }
            }
            return INSTANCE
        }
    }


}