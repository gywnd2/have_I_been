package com.udangtangtang.haveibeen.fragment

import android.app.Activity
import android.app.AlertDialog
import android.app.Application
import android.icu.text.AlphabeticIndex.Record
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.udangtangtang.haveibeen.R
import com.udangtangtang.haveibeen.activity.RecordDetailActivity
import com.udangtangtang.haveibeen.databinding.FragmentRecordViewBinding
import com.udangtangtang.haveibeen.repository.RecordRepository
import com.udangtangtang.haveibeen.util.ViewPagerAdapter
import com.udangtangtang.haveibeen.viewmodel.RecordViewModel
import com.udangtangtang.haveibeen.viewmodel.RecordViewModelFactory

class RecordViewFragment : Fragment() {
    companion object{
        private const val TAG="RecordViewFragment"
    }

    private var _binding : FragmentRecordViewBinding?=null
    private val binding get()=_binding!!
    private lateinit var recordViewModel : RecordViewModel
    private lateinit var dialog: AlertDialog
    private lateinit var builder: AlertDialog.Builder
    private lateinit var parentActivity : RecordDetailActivity
    private lateinit var db : RecordRepository


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding=DataBindingUtil.inflate(inflater, R.layout.fragment_record_view, container, false)
        parentActivity=activity as RecordDetailActivity
        db= RecordRepository(parentActivity.application)
        val selectedLatLng = parentActivity.intent.getDoubleArrayExtra("selectedLatLng")

        // UI 업데이트를 위한 Observer
        val factory =  RecordViewModelFactory(db, selectedLatLng!!)
        recordViewModel= ViewModelProvider(this, factory).get(RecordViewModel::class.java)
        binding.viewModel= recordViewModel
        binding.isEditing=false
        recordViewModel.currentRecord.observe(parentActivity, Observer{
            recordViewModel.setViewRecord(it)
            Log.d(TAG, "Record changed : "+it.toString())
        })

        // 전달받은 위/경도 정보를 ViewPager 어댑터로 전달
        // 같은 위/경도에 해당하는 모든 사진을 ViewPager에 추가
        binding.recordDetailViewpager2.adapter = ViewPagerAdapter(parentActivity, db,
            selectedLatLng, false)

//         Indicator 설정
        binding.recordDetailImageIndicator.setViewPager(binding.recordDetailViewpager2)
        binding.recordDetailImageIndicator.createIndicators(db.getSpecificLocationPictureCount(selectedLatLng[0], selectedLatLng[1]),0)

//         수정 클릭시 수정 시작
        binding.recordDetailButtonEdit.setOnClickListener {
            if (binding.isEditing!!){
                // 수정 확인
                builder = AlertDialog.Builder(parentActivity)
                builder.setTitle(R.string.record_detail_title_save)
                    .setMessage(R.string.record_detail_message_save)
                    .setPositiveButton(R.string.yes) { _, _ ->
                        // 입력 중지
                        binding.isEditing=false
                        // DB 업데이트
                        with(binding){ recordViewModel.updateRecord(recordDetailLocationName.text.toString(), recordDetailRating.rating, recordDetailComment.text.toString()) }

                        // 저장 알림
                        Toast.makeText(parentActivity, getString(R.string.saved), Toast.LENGTH_LONG)
                            .show()
                    }.setNegativeButton(R.string.no) { _, _ -> }

                // 확인창 표시
                dialog = builder.create()
                builder.show()
            }else{
                binding.isEditing=true
            }

        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding=null
    }
}