package com.udangtangtang.haveibeen.viewmodel

import android.app.Application
import android.icu.text.AlphabeticIndex.Record
import android.util.Log
import androidx.lifecycle.*

import com.udangtangtang.haveibeen.entity.RecordEntity
import com.udangtangtang.haveibeen.repository.RecordRepository
import kotlinx.coroutines.*

class RecordViewModel(private val db:RecordRepository, private val selectedLatLng : DoubleArray) : ViewModel(){
    private val _currentRecord=MutableLiveData<RecordEntity>()

    companion object {
        private const val TAG ="RecordViewModel"
    }

    init{
        runBlocking {
            _currentRecord.value=db.getRecord(selectedLatLng[0], selectedLatLng[1]).value
        }
    }

    val currentRecord : LiveData<RecordEntity>
        get()=_currentRecord

    fun updateRecord(locName: String, rating: Float, comment: String){
        _currentRecord.value!!.locationName =locName
        _currentRecord.value!!.rating=rating
        _currentRecord.value!!.comment=comment
        viewModelScope.launch {
            db.updateRecord(_currentRecord.value!!)
        }
    }

    fun setViewRecord(){
        viewModelScope.launch {
            if(async { db.isRecordExist(selectedLatLng[0], selectedLatLng[1]) }.await()){
                db.createRecord(selectedLatLng[0], selectedLatLng[1])
                _currentRecord.value=db.getRecord(selectedLatLng[0], selectedLatLng[1])!!.value
            }else{
                val record=db.getRecord(selectedLatLng[0], selectedLatLng[1])!!.value
                if(currentRecord.value!=record){
                    _currentRecord.value=record!!
                }
            }
        }
    }


}
//
class RecordViewModelFactory(val db:RecordRepository, val selectedLatLng : DoubleArray) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return RecordViewModel(db, selectedLatLng) as T
    }
}
