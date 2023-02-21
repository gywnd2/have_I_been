package com.udangtangtang.haveibeen.viewmodel

import android.icu.text.IDNA.Info
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.naver.maps.map.overlay.InfoWindow
import com.udangtangtang.haveibeen.entity.InfoWindowData
import com.udangtangtang.haveibeen.repository.RecordRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
class InfoWindowViewModel(val db: RecordRepository, val selectedLatLng : DoubleArray) : ViewModel() {
    private val _currentRecord=MutableLiveData<InfoWindowData>()

    companion object{
        private const val TAG="InfoWindowViewModel"
    }

    val currentRecord:LiveData<InfoWindowData>
        get() = _currentRecord

    init{
        runBlocking {
            _currentRecord.postValue(db.getInfoWindowData(selectedLatLng[0], selectedLatLng[1]).value)
            Log.d(TAG, _currentRecord.value.toString())
        }
    }

    fun setInfoWindow(info : InfoWindowData){
        if(_currentRecord.value!=info){
            _currentRecord.value=info
        }
        Log.d(TAG, _currentRecord.toString()+"\n"+info.toString())
    }

    class InfoWindowViewModelFactory(val db:RecordRepository, val selectedLatLng: DoubleArray):
            ViewModelProvider.Factory{
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return InfoWindowViewModel(db, selectedLatLng) as T
        }
    }

}