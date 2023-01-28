package com.udangtangtang.haveibeen.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.udangtangtang.haveibeen.R
import com.udangtangtang.haveibeen.activity.RecordDetailActivity
import com.udangtangtang.haveibeen.databinding.FragmentPictureViewBinding
import com.udangtangtang.haveibeen.databinding.FragmentRecordViewBinding
import com.udangtangtang.haveibeen.util.ViewPagerAdapter

class PictureViewFragment : Fragment(){
    private lateinit var binding : FragmentPictureViewBinding
    private lateinit var parentActivity : RecordDetailActivity

    companion object{
        private const val TAG="PictureViewFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= FragmentPictureViewBinding.inflate(layoutInflater)
        parentActivity=activity as RecordDetailActivity

        // 전달받은 위/경도 정보를 ViewPager 어댑터로 전달
        // 같은 위/경도에 해당하는 모든 사진을 ViewPager에 추가
        binding.recordDetailViewpager2.adapter = ViewPagerAdapter(parentActivity, parentActivity.db, parentActivity.selectedLatLng!!, true)

//         Indicator 설정
        binding.recordDetailImageIndicator.setViewPager(binding.recordDetailViewpager2)
        binding.recordDetailImageIndicator.createIndicators(parentActivity.db.getSpecificLocationPictureCount(parentActivity.selectedLatLng[0], parentActivity.selectedLatLng[1]),0)


        return inflater.inflate(R.layout.fragment_picture_view, container, false)
    }
}