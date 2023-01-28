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


class RecordDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRecordDetailBinding
    private lateinit var fragmentManager: FragmentManager
    lateinit var selectedLatLng: DoubleArray
    lateinit var db : RecordRepository
    lateinit var record : RecordEntity

    companion object{
        private const val TAG = "RecordDetailActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecordDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fragmentManager=supportFragmentManager
        // DB 연결
        db=RecordRepository(application)

        val recordFragment=RecordViewFragment()
        val pictureFragment=PictureViewFragment()

        // MainActivity로 부터 fileName 받아오기
        selectedLatLng = intent.getDoubleArrayExtra("selectedLatLng")!!
        record=db.getRecord(selectedLatLng!![0], selectedLatLng[1])

        // 액티비티 타이틀 설정
        title = if (record.locationName == null) getString(R.string.no_location_info) else getString(
            R.string.no_location_info
        )

        // Fragment 추가
        fragmentManager.beginTransaction()
            .add(R.id.container_record_fragments, recordFragment)
            .commit()

        // TODO : 사진 터치 시 프래그먼트 전환

    }

    fun changeFragment(){
        fragmentManager.beginTransaction()
            .replace(R.id.container_record_fragments, PictureViewFragment)
            .addToBackStack(null)
            .commit()
    }
}

