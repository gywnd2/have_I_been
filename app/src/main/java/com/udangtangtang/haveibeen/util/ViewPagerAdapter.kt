package com.udangtangtang.haveibeen.util

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.udangtangtang.haveibeen.R
import com.udangtangtang.haveibeen.activity.RecordDetailActivity
import com.udangtangtang.haveibeen.databinding.Viewpager2RecordDetailBinding
import com.udangtangtang.haveibeen.repository.RecordRepository
import com.udangtangtang.haveibeen.util.ViewPagerAdapter.ViewHolderPage
import java.io.File

class ViewPagerAdapter(
    private val context: Context,
    private val db: RecordRepository,
    private val latLng: DoubleArray,
    private val isPictureView: Boolean
) : RecyclerView.Adapter<ViewHolderPage>() {

    private lateinit var binding : Viewpager2RecordDetailBinding
    private val pictures : List<String>
    companion object{
        private const val TAG ="ViewPagerAdapter"
    }

    init {
        pictures=db.getPictureOfSpecificLocation(latLng[0], latLng[1])
        Log.d(TAG, pictures.toString()+"pos : "+latLng[0]+"/"+latLng[1])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderPage {
        binding= Viewpager2RecordDetailBinding.inflate(LayoutInflater.from(context), parent, false)
        binding.viewpager2RecordDetailImage.setOnClickListener{
            if(!isPictureView) (parent.context as RecordDetailActivity).changeFragment()
        }
        return ViewHolderPage(binding.root)
    }

    override fun onBindViewHolder(holder: ViewHolderPage, position: Int) {
        holder.onBind(pictures[position])
        Log.d(TAG, "binding "+pictures[position]+" at position : "+position)
    }

    override fun getItemCount(): Int {
        return pictures.size
    }

    inner class ViewHolderPage internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun onBind(filename: String?) {
            Log.d(TAG, "binding "+filename+" at position : "+position)
            // 사진 파일 전체 경로로 이미지 설정
            Glide.with(context)
                .load(File(filename!!))
                .placeholder(R.drawable.image)
                .into(binding.viewpager2RecordDetailImage)
        }
    }
}