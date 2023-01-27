package com.udangtangtang.haveibeen

import android.app.AlertDialog
import android.app.Application
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.common.internal.safeparcel.SafeParcelable
import com.udangtangtang.haveibeen.viewmodel.RecordViewModel
import com.udangtangtang.haveibeen.databinding.ActivityRecordDetailBinding
import com.udangtangtang.haveibeen.entity.RecordEntity
import com.udangtangtang.haveibeen.repository.RecordRepository
import com.udangtangtang.haveibeen.util.ViewPagerAdapter
import com.udangtangtang.haveibeen.viewmodel.RecordViewModelFactory


class RecordDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRecordDetailBinding
    private lateinit var recordViewModel : RecordViewModel
    private lateinit var db : RecordRepository
    private lateinit var dialog: AlertDialog
    private lateinit var builder: AlertDialog.Builder

    companion object{
        private const val TAG = "RecordDetailActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView<ActivityRecordDetailBinding>(this, R.layout.activity_record_detail)

        // DB 연결
        db=RecordRepository(application)

        // MainActivity로 부터 fileName 받아오기
        val selectedLatLng = intent.getDoubleArrayExtra("selectedLatLng")

        // UI 업데이트를 위한 Observer
        val factory =  RecordViewModelFactory(db, selectedLatLng!!)
        recordViewModel=ViewModelProvider(this, factory).get(RecordViewModel::class.java)
        binding.viewModel=recordViewModel!!
        binding.isEditing=false
        recordViewModel.currentRecord.observe(this, Observer{
            recordViewModel.setViewRecord(it)
            Log.d(TAG, "Record changed : "+it.toString())
        })

        // 전달받은 위/경도 정보를 ViewPager 어댑터로 전달
        // 같은 위/경도에 해당하는 모든 사진을 ViewPager에 추가
        binding.recordDetailViewpager2.adapter = ViewPagerAdapter(this, db, selectedLatLng!!)

//         액티비티 타이틀 설정
        title = if (binding.viewModel!!.currentRecord.value?.locationName == null) getString(R.string.no_location_info) else getString(R.string.no_location_info)

//         SafeParcelable.Indicator 설정
        binding.recordDetailImageIndicator.setViewPager(binding.recordDetailViewpager2)
        binding.recordDetailImageIndicator.createIndicators(db.getSpecificLocationPictureCount(selectedLatLng[0], selectedLatLng[1]),0)

//         수정 클릭시 수정 시작
        binding.recordDetailButtonEdit.setOnClickListener {
            if (binding.isEditing!!){
                // 수정 확인
                builder = AlertDialog.Builder(this@RecordDetailActivity)
                builder.setTitle(R.string.record_detail_title_save)
                    .setMessage(R.string.record_detail_message_save)
                    .setPositiveButton(R.string.yes) { _, _ ->
                        // 입력 중지
                        binding.isEditing=false
                        // DB 업데이트
                        with(binding){ recordViewModel.updateRecord(recordDetailLocationName.text.toString(), recordDetailRating.rating, recordDetailComment.text.toString()) }

                        // 저장 알림
                        Toast.makeText(applicationContext, getString(R.string.saved), Toast.LENGTH_LONG)
                            .show()
                    }.setNegativeButton(R.string.no) { _, _ -> }

                // 확인창 표시
                dialog = builder.create()
                builder.show()
            }else{
                binding.isEditing=true
            }

        }
    }
}

