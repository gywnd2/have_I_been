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
import com.udangtangtang.haveibeen.databinding.ViewpagerImageHolderBinding
import com.udangtangtang.haveibeen.repository.RecordRepository
import com.udangtangtang.haveibeen.util.RecordPictureAdapter.RecordPictureHolder
import kotlinx.coroutines.runBlocking
import java.io.File

class RecordPictureAdapter(
    private val context: Context,
    private val db: RecordRepository,
    private val latLng: DoubleArray,
    private val isPictureView: Boolean
) : RecyclerView.Adapter<RecordPictureHolder>() {

    private lateinit var binding : ViewpagerImageHolderBinding
    private var pictures : List<String>
    companion object{
        private const val TAG ="RecordPictureAdapter"
    }

    init {
        runBlocking {
            pictures=db.getPictureOfSpecificLocation(latLng[0], latLng[1])
            Log.d(TAG, pictures.toString()+"pos : "+latLng[0]+"/"+latLng[1])
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordPictureHolder {
        binding= ViewpagerImageHolderBinding.inflate(LayoutInflater.from(context), parent, false)
        binding.viewpager2RecordDetailImage.setOnClickListener{
            if(!isPictureView) (parent.context as RecordDetailActivity).changeFragment()
        }
        return RecordPictureHolder(binding.root)
    }

    override fun onBindViewHolder(holder: RecordPictureHolder, position: Int) {
        holder.onBind(pictures[position])
        Log.d(TAG, "binding "+pictures[position]+" at position : "+position)
    }

    override fun getItemCount(): Int {
        return pictures.size
    }

    inner class RecordPictureHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun onBind(filename: String?) {
            // 사진 파일 전체 경로로 이미지 설정
            Glide.with(context)
                .load(File(filename!!))
                .placeholder(R.drawable.image)
                .into(binding.viewpager2RecordDetailImage)
        }
    }
}