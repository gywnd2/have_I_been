package com.udangtangtang.haveibeen.repository

import android.annotation.SuppressLint
import android.app.Application
import android.icu.text.AlphabeticIndex.Record
import android.util.Log
import androidx.room.Query
import com.udangtangtang.haveibeen.dao.PictureDao
import com.udangtangtang.haveibeen.dao.RecordDao
import com.udangtangtang.haveibeen.database.PictureDatabase
import com.udangtangtang.haveibeen.database.RecordDatabase
import com.udangtangtang.haveibeen.entity.AddressRankTuple
import com.udangtangtang.haveibeen.entity.LatLngTuple
import com.udangtangtang.haveibeen.entity.PictureEntity
import com.udangtangtang.haveibeen.entity.RecordEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RecordRepository(application: Application) {
    private val recordDao : RecordDao
    private val pictureDao : PictureDao
    private val TAG = "repository"
    init{
        var recordDB=RecordDatabase.getInstance(application)
        var pictureDB=PictureDatabase.getInstance(application)
        recordDao=recordDB!!.getRecordDao()
        pictureDao=pictureDB!!.getPictureDao()
    }

    fun getRecord(latitude : Double, longtitude : Double) : RecordEntity{
        if(recordDao.isExist(latitude, longtitude)){
            return recordDao.getEntity(latitude, longtitude)
        }else{
            val record=RecordEntity(latitude, longtitude, null, pictureDao.getAddress(latitude, longtitude), pictureDao.getDatetime(latitude, longtitude), null, null)
            createRecord(record)
            return record
        }
    }

    fun createRecord(record : RecordEntity){
        recordDao.insert(record)
    }

    fun deleteRecord(latitude : Double, longtitude : Double){
        recordDao.delete(latitude, longtitude)
    }

    // TODO : 세부 수정 가능하도록?
    fun updateRecord(record:RecordEntity){
        recordDao.update(record)
    }

    fun getAddressCount() : List<AddressRankTuple>{
        return pictureDao.getAddressCount()
    }

//    fun addPicture(pictureList : List<PictureEntity>){
//        CoroutineScope(Dispatchers.IO).launch {
//            for (picture in pictureList){
//                if(!pictureDao.isExist(picture.fileName)){
//                    pictureDao.insert(picture)
//                    Log.d(TAG, picture.toString())
//                }else{
//                    Log.d(TAG, picture.fileName+" 은 이미 존재합니다.")
//                }
//            }
//        }
//    }

    fun addPicture(picture : PictureEntity){
        CoroutineScope(Dispatchers.IO).launch {
            pictureDao.insert(picture)
            Log.d(TAG, picture.toString())
        }
    }

    fun isExistPicture(latitude: Double, longtitude: Double, fileName : String):Boolean{
        return pictureDao.isExist(latitude, longtitude, fileName)
    }

    fun updatePictureAddress(latitude: Double, longtitude: Double, address: String){
        if(address!=null){
            pictureDao.updateAddress(latitude, longtitude, address)
            Log.d(TAG, "record address updated : "+pictureDao.getAddress(latitude, longtitude))
        }else{
            Log.d(TAG, "null address received")
        }
    }

    fun getTotalPictureCount() : Int{
        return pictureDao.getPictureNumbers()
    }

    fun getPictureCoordination(filePath : String) : LatLngTuple {
        return pictureDao.getPictureLatLng(filePath)
    }

    fun getPictureList() : List<String>{
        return pictureDao.getFileList()
    }
}