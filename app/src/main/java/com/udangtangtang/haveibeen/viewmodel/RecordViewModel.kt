package com.udangtangtang.haveibeen.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bumptech.glide.Glide.init

import com.udangtangtang.haveibeen.entity.RecordEntity
import com.udangtangtang.haveibeen.repository.RecordRepository

class RecordViewModel(application : Application, latitude : Double, longtitude : Double) : AndroidViewModel(application){
    private val db : RecordRepository
    private val _currentRecord=MutableLiveData<RecordEntity>()

    val currentRecord : LiveData<RecordEntity>
        get()=_currentRecord

    init{
        db= RecordRepository(application)
        _currentRecord.value=db.getRecord(latitude, longtitude)
    }

    fun updateRecord(locName: String, rating: Float, comment: String){
        _currentRecord.value!!.locationName =locName
        _currentRecord.value!!.rating=rating
        _currentRecord.value!!.comment=comment
        db.updateRecord(_currentRecord.value!!)
    }

    fun setViewRecord(record : RecordEntity){
        _currentRecord.value=record
    }
}