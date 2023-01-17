package com.udangtangtang.haveibeen.dao

import androidx.room.Query
import com.udangtangtang.haveibeen.entity.PictureEntity
import com.udangtangtang.haveibeen.entity.RecordEntity

interface RecordDao {
    @Query("SELECT * FROM recordDB WHERE latitude LIKE :lat AND longtitude LIKE :lng")
    fun getRecord(lat : Double, lng : Double): RecordEntity
}