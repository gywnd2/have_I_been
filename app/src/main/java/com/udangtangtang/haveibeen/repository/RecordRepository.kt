package com.udangtangtang.haveibeen.repository

import android.app.Application
import android.util.Log
import com.udangtangtang.haveibeen.dao.PictureDao
import com.udangtangtang.haveibeen.dao.RecordDao
import com.udangtangtang.haveibeen.database.PictureDatabase
import com.udangtangtang.haveibeen.database.RecordDatabase
import com.udangtangtang.haveibeen.model.AddressRankTuple
import com.udangtangtang.haveibeen.model.LatLngTuple
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
        val recordDB=RecordDatabase.getInstance(application)
        val pictureDB=PictureDatabase.getInstance(application)
        recordDao=recordDB!!.getRecordDao()
        pictureDao=pictureDB!!.getPictureDao()
    }

    suspend fun getRecord(latitude : Double, longtitude : Double) : RecordEntity{
        return if(recordDao.isExist(latitude, longtitude)){
            recordDao.getEntity(latitude, longtitude)
        }else{
            val record=RecordEntity(latitude, longtitude, null, pictureDao.getAddress(latitude, longtitude), pictureDao.getDatetime(latitude, longtitude), null, null)
            createRecord(record)
            record
        }
    }

    suspend fun createRecord(record : RecordEntity){
        recordDao.insert(record)
    }

    suspend fun deleteRecord(latitude : Double, longtitude : Double){
        recordDao.delete(latitude, longtitude)
    }

    // TODO : 세부 수정 가능하도록?
    suspend fun updateRecord(record:RecordEntity){
        recordDao.update(record)
    }

    suspend fun getAddressCount() : List<AddressRankTuple>{
        return pictureDao.getAddressCount()
    }

    suspend fun addPicture(picture : PictureEntity){
        CoroutineScope(Dispatchers.IO).launch {
            pictureDao.insert(picture)
            Log.d(TAG, picture.toString())
        }
    }

    suspend fun isExistPicture(latitude: Double, longtitude: Double, fileName : String):Boolean{
        return pictureDao.isExist(latitude, longtitude, fileName)
    }

    suspend fun updatePictureAddress(latitude: Double, longtitude: Double, address: String){
        if(address!=null){
            pictureDao.updateAddress(latitude, longtitude, address)
            Log.d(TAG, "record address updated : "+pictureDao.getAddress(latitude, longtitude))
        }else{
            Log.d(TAG, "null address received")
        }
    }

    suspend fun getTotalPictureCount() : Int{
        return pictureDao.getPictureNumbers()
    }

    suspend fun getPictureCoordination(filePath : String) : LatLngTuple {
        return pictureDao.getPictureLatLng(filePath)
    }

    suspend fun getPictureList() : List<String>{
        return pictureDao.getFileList()
    }

    suspend fun getSpecificLocationPictureCount(latitude: Double, longtitude: Double):Int{
        return pictureDao.getFileCountOnLocation(latitude, longtitude)
    }

    suspend fun getPictureOfSpecificLocation(latitude: Double, longtitude: Double):List<String>{
        return pictureDao.getFileOnLocation(latitude, longtitude)
    }

    suspend fun getAdderessRanking():List<String>{
        return pictureDao.getAddressCountByGroup()
    }
}