package com.udangtangtang.haveibeen.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class RecordViewModelFactory(private val application: Application, private val selectedLatLng : DoubleArray) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(RecordViewModel::class.java)){
            return RecordViewModel(application, selectedLatLng[0], selectedLatLng[1]) as T
        }
        return super.create(modelClass)
    }
}