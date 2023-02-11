package com.udangtangtang.haveibeen.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.udangtangtang.haveibeen.R
import com.udangtangtang.haveibeen.databinding.ActivityRecordDetailBinding
import com.udangtangtang.haveibeen.entity.RecordEntity
import com.udangtangtang.haveibeen.fragment.PictureViewFragment
import com.udangtangtang.haveibeen.fragment.RecordViewFragment
import com.udangtangtang.haveibeen.repository.RecordRepository
import kotlinx.coroutines.*


class RecordDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRecordDetailBinding
    private lateinit var fragmentManager: FragmentManager
    private lateinit var db : RecordRepository
    lateinit var pictureFragment : PictureViewFragment
    lateinit var recordFragment : RecordViewFragment

    companion object{
        private const val TAG = "RecordDetailActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecordDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        db= RecordRepository(application)
            fragmentManager=supportFragmentManager
            // MainActivity로 부터 fileName 받아오기
            val selectedLatLng = intent.getDoubleArrayExtra("selectedLatLng")!!
                    CoroutineScope(Dispatchers.IO).launch {
                async {
                    val record=db.getRecord(selectedLatLng!![0], selectedLatLng[1])
                    // 액티비티 타이틀 설정
                    this@RecordDetailActivity.title = if (record.locationName == null) getString(R.string.no_location_info) else getString(R.string.no_location_info)
            }.await()
        }



        pictureFragment=PictureViewFragment()
        recordFragment=RecordViewFragment()

        // Fragment 추가
        fragmentManager.beginTransaction()
            .add(R.id.container_record_fragments, recordFragment)
            .commit()

        // TODO : 사진 터치 시 프래그먼트 전환

    }

    fun changeFragment(){
        fragmentManager.beginTransaction()
            .replace(R.id.container_record_fragments, pictureFragment)
            .addToBackStack(null)
            .commit()
    }
}

