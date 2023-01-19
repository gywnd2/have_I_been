package com.udangtangtang.haveibeen.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.udangtangtang.haveibeen.dao.PictureDao
import com.udangtangtang.haveibeen.entity.PictureEntity

@Database(entities= arrayOf(PictureEntity::class), version=1)
abstract class PictureDatabase :RoomDatabase(){
    abstract fun getPictureDao() : PictureDao

    companion object{
        var INSTANCE: PictureDatabase?=null

        fun getInstance(context: Context) : PictureDatabase? {
            if(INSTANCE ==null){
                synchronized(PictureDatabase::class){
                    INSTANCE = Room.databaseBuilder(context.applicationContext, PictureDatabase::class.java, "Pictures.db")
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