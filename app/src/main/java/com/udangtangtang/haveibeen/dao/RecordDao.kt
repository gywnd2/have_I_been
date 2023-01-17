package com.udangtangtang.haveibeen.dao

import androidx.room.Query
import com.udangtangtang.haveibeen.entity.PictureEntity

interface RecordDao {
    @Query("SELECT * FROM picturedata")
    fun getRecord(): PictureEntity
}