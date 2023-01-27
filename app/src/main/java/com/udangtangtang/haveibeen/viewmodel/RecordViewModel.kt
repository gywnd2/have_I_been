package com.udangtangtang.haveibeen.viewmodel

import android.app.Application
import android.icu.text.AlphabeticIndex.Record
import android.util.Log
import androidx.lifecycle.*

import com.udangtangtang.haveibeen.entity.RecordEntity
import com.udangtangtang.haveibeen.repository.RecordRepository

class RecordViewModel(val db:RecordRepository, val selectedLatLng : DoubleArray) : ViewModel(){
    private val _currentRecord=MutableLiveData<RecordEntity>()

    companion object {
        private const val TAG ="RecordViewModel"
    }

    val currentRecord : LiveData<RecordEntity>
        get()=_currentRecord

    init{
        _currentRecord.value=db.getRecord(selectedLatLng[0], selectedLatLng[1])
    }

    fun updateRecord(locName: String, rating: Float, comment: String){
        _currentRecord.value!!.locationName =locName
        _currentRecord.value!!.rating=rating
        _currentRecord.value!!.comment=comment
        db.updateRecord(_currentRecord.value!!)
    }

    fun setViewRecord(record : RecordEntity){
        if (_currentRecord.value!=record){
            _currentRecord.value=record
        }
        Log.d(TAG, _currentRecord.toString()+"\n"+record.toString())
    }


}
//
class RecordViewModelFactory(val db:RecordRepository, val selectedLatLng : DoubleArray) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return RecordViewModel(db, selectedLatLng) as T
    }
}
