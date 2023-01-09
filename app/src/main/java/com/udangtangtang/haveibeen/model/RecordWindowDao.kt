package com.udangtangtang.haveibeen.model

import androidx.room.Query

interface RecordWindowDao {
    @Query("SELECT * FROM picturedata")
    fun getRecord():PictureData
}