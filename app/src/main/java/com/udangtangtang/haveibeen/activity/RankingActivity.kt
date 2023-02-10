package com.udangtangtang.haveibeen.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.udangtangtang.haveibeen.R
import com.udangtangtang.haveibeen.databinding.ActivityRankingBinding
import com.udangtangtang.haveibeen.database.PictureDatabase
import com.udangtangtang.haveibeen.repository.RecordRepository
import com.udangtangtang.haveibeen.util.RankingCardAdapter
import java.util.*

class RankingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRankingBinding
    private lateinit var db : RecordRepository
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRankingBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        setTitle(R.string.ranking_title)

        // DB접근을 위한 Helper 초기화
        db= RecordRepository(application)
        binding.viewpager2Ranking.adapter=RankingCardAdapter(this, db)

    }
}