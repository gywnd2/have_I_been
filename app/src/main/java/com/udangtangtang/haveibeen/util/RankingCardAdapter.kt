package com.udangtangtang.haveibeen.util

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.udangtangtang.haveibeen.databinding.ViewpagerRankHolderBinding
import com.udangtangtang.haveibeen.repository.RecordRepository
import kotlinx.coroutines.runBlocking

class RankingCardAdapter(
    private val context: Context,
    private val db: RecordRepository,
) : RecyclerView.Adapter<RankingCardAdapter.RankCardHolder>() {

    companion object{
        private const val TAG ="RankingCardAdapter"
    }

    private lateinit var binding : ViewpagerRankHolderBinding
    private var rankInfo : List<String>

    init {
        runBlocking {
            rankInfo=db.getAdderessRanking()
            Log.d(TAG, rankInfo.toString())
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RankCardHolder {
        binding= ViewpagerRankHolderBinding.inflate(LayoutInflater.from(context), parent, false)
        return RankCardHolder(binding.root)
    }

    override fun onBindViewHolder(holder: RankCardHolder, position: Int) {
        holder.onBind(position)
    }

    override fun getItemCount(): Int {
        return rankInfo.size
    }

    inner class RankCardHolder internal constructor(itemView:View):RecyclerView.ViewHolder(itemView){
        fun onBind(position:Int){
            binding.textRankingHolderRank.text=(position+1).toString()
            binding.textRankingHolderPictureCount.text="TODO"
//            binding.textRankingHolderAddress.text=rankInfo[position].split(" ")
        }
    }
}