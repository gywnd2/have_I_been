package com.udangtangtang.haveibeen.ViewModel

import android.icu.text.AlphabeticIndex.Record
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.udangtangtang.haveibeen.entity.RecordEntity

class RecordViewModel : ViewModel(){
    val currentRecord : MutableLiveData<RecordEntity> by lazy{
        MutableLiveData<RecordEntity>()
    }

}