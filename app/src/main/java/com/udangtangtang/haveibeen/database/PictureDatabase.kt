package com.udangtangtang.haveibeen.database

import android.content.Context
import android.location.Address
import androidx.room.*
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.udangtangtang.haveibeen.dao.PictureDao
import com.udangtangtang.haveibeen.entity.PictureEntity
import com.udangtangtang.haveibeen.util.AddressTypeConverter
import kotlinx.coroutines.CoroutineScope

@Database(entities= arrayOf(PictureEntity::class), version=1)
abstract class PictureDatabase :RoomDatabase(){
    abstract fun getPictureDao() : PictureDao

    companion object{
        @Volatile
        var INSTANCE: PictureDatabase?=null

        suspend fun getInstance(context: Context, scope:CoroutineScope) : PictureDatabase? {
            return INSTANCE?: synchronized(this){
                INSTANCE=Room.databaseBuilder(context.applicationContext, PictureDatabase::class.java, "Pictures.db")
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE
            }
        }
    }
}