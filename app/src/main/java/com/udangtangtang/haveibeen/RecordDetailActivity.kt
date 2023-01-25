package com.udangtangtang.haveibeen

import android.app.AlertDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import com.udangtangtang.haveibeen.databinding.ActivityRecordDetailBinding
import com.udangtangtang.haveibeen.repository.RecordRepository
import com.udangtangtang.haveibeen.util.ViewPagerAdapter


class RecordDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRecordDetailBinding
    private lateinit var db : RecordRepository
    private lateinit var dialog: AlertDialog
    private lateinit var builder: AlertDialog.Builder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_record_detail)
        setContentView(binding.root)

        // DB 연결
        db=RecordRepository(application)

        // MainActivity로 부터 fileName 받아오기
        val selectedLatLng = intent.getDoubleArrayExtra("selectedLatLng")

        // 전달받은 위/경도 정보를 ViewPager 어댑터로 전달
        // 같은 위/경도에 해당하는 모든 사진을 ViewPager에 추가
        binding.recordDetailViewpager2.adapter = ViewPagerAdapter(this, selectedLatLng!!)

        // 데이터 가져오기
        var queryRecord=db.getRecord(selectedLatLng[0], selectedLatLng[1])
        binding.record=queryRecord
        binding.isEditing=false

        // 액티비티 타이틀 설정
        title = if (queryRecord.locationName == null) getString(R.string.no_location_info) else queryRecord.locationName

        // Indicator 설정
        binding.recordDetailImageIndicator.setViewPager(binding.recordDetailViewpager2)
        binding.recordDetailImageIndicator.createIndicators(db.getSpecificLocationPictureCount(selectedLatLng[0], selectedLatLng[1]),0)

        // 수정 클릭시 수정 시작
        binding.recordDetailButtonEdit.setOnClickListener {
            if (binding.isEditing!!){
                // 수정 확인
                builder = AlertDialog.Builder(this@RecordDetailActivity)
                builder.setTitle(R.string.record_detail_title_save)
                    .setMessage(R.string.record_detail_message_save)
                    .setPositiveButton(R.string.yes) { _, _ ->
                        // 입력 중지
                        binding.isEditing=false
                        queryRecord.locationName= binding.recordDetailLocationName.text.toString()
                        queryRecord.rating=binding.recordDetailRating.rating
                        queryRecord.comment=binding.recordDetailComment.text.toString()

                        // DB 업데이트
                        db.updateRecord(queryRecord)

                        // 저장 시에는 다시 조회하여 출력
                        binding.record=db.getRecord(selectedLatLng[0], selectedLatLng[1])

                        // 데이터 내용 표시
                        Toast.makeText(applicationContext, getString(R.string.saved), Toast.LENGTH_LONG)
                            .show()
                    }
                    .setNegativeButton(R.string.no) { _, _ -> }

                // 확인창 표시
                dialog = builder.create()
                builder.show()
            }else{
                binding.isEditing=true
                Toast.makeText(this, binding.isEditing.toString(),Toast.LENGTH_SHORT).show()
            }

        }
    }
}