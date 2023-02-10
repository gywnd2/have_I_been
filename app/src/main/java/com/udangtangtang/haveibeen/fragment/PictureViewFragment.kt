package com.udangtangtang.haveibeen.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.udangtangtang.haveibeen.activity.RecordDetailActivity
import com.udangtangtang.haveibeen.databinding.FragmentPictureViewBinding
import com.udangtangtang.haveibeen.repository.RecordRepository
import com.udangtangtang.haveibeen.util.RecordPictureAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PictureViewFragment : Fragment(){
    private var _binding : FragmentPictureViewBinding?=null
    private val binding get() = _binding!!
    private lateinit var parentActivity : RecordDetailActivity
    private lateinit var db : RecordRepository

    companion object{
        private const val TAG="PictureViewFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding= FragmentPictureViewBinding.inflate(inflater, container, false)
        parentActivity=activity as RecordDetailActivity
        db= RecordRepository(parentActivity.application)
        val selectedLatLng = parentActivity.intent.getDoubleArrayExtra("selectedLatLng")

        // 전달받은 위/경도 정보를 ViewPager 어댑터로 전달
        // 같은 위/경도에 해당하는 모든 사진을 ViewPager에 추가
        binding.recordDetailViewpager2.adapter = RecordPictureAdapter(parentActivity, db, selectedLatLng!!, true)
//         Indicator 설정
        binding.recordDetailImageIndicator.setViewPager(binding.recordDetailViewpager2)
        CoroutineScope(Dispatchers.Main).launch {
            binding.recordDetailImageIndicator.createIndicators(db.getSpecificLocationPictureCount(selectedLatLng[0], selectedLatLng[1]),0)
        }


        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding=null
    }
}