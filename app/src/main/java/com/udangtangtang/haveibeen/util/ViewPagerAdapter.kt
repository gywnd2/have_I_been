package com.udangtangtang.haveibeen.util

import android.app.Application
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.udangtangtang.haveibeen.R
import com.udangtangtang.haveibeen.databinding.Viewpager2RecordDetailBinding
import com.udangtangtang.haveibeen.repository.RecordRepository
import com.udangtangtang.haveibeen.util.ViewPagerAdapter.ViewHolderPage
import java.io.File

class ViewPagerAdapter(private val context: Context, private val db:RecordRepository, private val latLng: DoubleArray) : RecyclerView.Adapter<ViewHolderPage>() {

    private val binding : Viewpager2RecordDetailBinding
    private val pictures : List<String>
    companion object{
        private const val TAG ="ViewPagerAdapter"
    }

    init {
        binding= Viewpager2RecordDetailBinding.inflate(LayoutInflater.from(context))
        pictures=db.getPictureOfSpecificLocation(latLng[0], latLng[1])
        Log.d(TAG, pictures.toString())
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderPage {
        val view = LayoutInflater.from(context).inflate(R.layout.viewpager2_record_detail, parent, false)
        return ViewHolderPage(view)
    }

    override fun onBindViewHolder(holder: ViewHolderPage, position: Int) {
        holder.onBind(pictures[position])
    }

    override fun getItemCount(): Int {
        return pictures.size
    }

    inner class ViewHolderPage internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun onBind(filename: String?) {
            // 사진 파일 전체 경로로 이미지 설정
            Glide.with(context)
                .load(File(filename!!))
                .into(binding.viewpager2RecordDetailImage)
        }
    }
}