package com.udangtangtang.haveibeen.repository

import android.app.Application
import android.icu.text.AlphabeticIndex.Record
import android.location.Address
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import com.google.gson.Gson
import com.udangtangtang.haveibeen.dao.PictureDao
import com.udangtangtang.haveibeen.dao.RecordDao
import com.udangtangtang.haveibeen.database.PictureDatabase
import com.udangtangtang.haveibeen.database.RecordDatabase
import com.udangtangtang.haveibeen.entity.InfoWindowData
import com.udangtangtang.haveibeen.entity.PictureEntity
import com.udangtangtang.haveibeen.entity.RecordEntity
import com.udangtangtang.haveibeen.model.AddressRankTuple
import com.udangtangtang.haveibeen.model.LatLngTuple
import kotlinx.coroutines.*
import kotlinx.coroutines.selects.select

class RecordRepository(application: Application) {
    private lateinit var recordDao: RecordDao
    private lateinit var pictureDao: PictureDao
    private val TAG = "repository"

    init {
        runBlocking {
            val recordDB=async{
                RecordDatabase.getInstance(application, this)
            }.await()
            recordDao = recordDB!!.getRecordDao()
            val pictureDB=async{
                PictureDatabase.getInstance(application, this)
            }.await()
            pictureDao = pictureDB!!.getPictureDao()
        }
    }

    fun getAllRecords():List<RecordEntity>{
        return recordDao.getAllData()
    }

    suspend fun isRecordExist(latitude: Double, longtitude: Double):Boolean {
        return recordDao.isExist(latitude, longtitude)
    }

    fun getRecord(latitude: Double, longtitude: Double): LiveData<RecordEntity> {
        return recordDao.getEntity(latitude, longtitude)
    }

    suspend fun createRecord(latitude: Double, longtitude: Double) {
        withContext(Dispatchers.IO){
            recordDao.insert(
                RecordEntity(
                    latitude,
                    longtitude,
                    null,
                    pictureDao.getAddress(latitude, longtitude),
                    pictureDao.getDatetime(latitude, longtitude),
                    null,
                    null
                )
            )
        }
    }

    suspend fun deleteRecord(latitude: Double, longtitude: Double) {
        withContext(Dispatchers.IO){
            recordDao.delete(latitude, longtitude)
        }
    }

    // TODO : 세부 수정 가능하도록?
    suspend fun updateRecord(record: RecordEntity) {
        withContext(Dispatchers.IO){
            recordDao.update(record)
        }
    }

    suspend fun getAddressCount(): List<AddressRankTuple> {
        return CoroutineScope(Dispatchers.IO).async {
            pictureDao.getAddressCount()
        }.await()
    }

    suspend fun addPicture(picture: PictureEntity) {
        withContext(Dispatchers.IO) {
            pictureDao.insert(picture)
            Log.d(TAG, picture.toString())
        }
    }

    suspend fun isExistPicture(latitude: Double, longtitude: Double, fileName: String): Boolean {
        return CoroutineScope(Dispatchers.IO).async {
            pictureDao.isExist(latitude, longtitude, fileName)
        }.await()
    }

    suspend fun updatePictureAddress(latitude: Double, longtitude: Double, address: String) {
        withContext(Dispatchers.IO){
            if (address != null) {
                Log.d(TAG, "update address of : "+pictureDao.getFileOnLocation(latitude,longtitude)+" to : "+ address)
                pictureDao.updateAddress(latitude, longtitude, address)
            } else {
                Log.d(TAG, "null address received")
            }
        }
    }

    suspend fun getTotalPictureCount(): Int {
        return CoroutineScope(Dispatchers.IO).async {
            pictureDao.getPictureNumbers()
        }.await()
    }

    suspend fun getPictureCoordination(filePath: String): LatLngTuple {
        return CoroutineScope(Dispatchers.IO).async {
            pictureDao.getPictureLatLng(filePath)
        }.await()
    }

    suspend fun getPictureList(): List<String> {
        return CoroutineScope(Dispatchers.IO).async {
            pictureDao.getFileList()
        }.await()
    }

    suspend fun getSpecificLocationPictureCount(latitude: Double, longtitude: Double): Int {
        return CoroutineScope(Dispatchers.IO).async {
            pictureDao.getFileCountOnLocation(latitude, longtitude)
        }.await()
    }

    suspend fun getPictureOfSpecificLocation(latitude: Double, longtitude: Double): List<String> {
        return CoroutineScope(Dispatchers.IO).async {
            pictureDao.getFileOnLocation(latitude, longtitude)
        }.await()
    }

    suspend fun getAdderessRanking(): List<AddressRankTuple> {
        return CoroutineScope(Dispatchers.IO).async {
            pictureDao.getAddressCountByGroup()
        }.await()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    suspend fun getInfoWindowData(latitude: Double, longtitude: Double): LiveData<InfoWindowData> {
        return CoroutineScope(Dispatchers.IO).async {
            if(recordDao.isExist(latitude, longtitude)){
                recordDao.getInfoWindowData(latitude, longtitude)
            }else{
                pictureDao.getInfoWindowData(latitude, longtitude)
            }
        }.await()
    }
}