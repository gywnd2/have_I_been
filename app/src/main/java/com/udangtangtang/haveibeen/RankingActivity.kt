package com.udangtangtang.haveibeen

import com.udangtangtang.haveibeen.dao.DBHelper
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.udangtangtang.haveibeen.databinding.ActivityRankingBinding
import java.util.*

class RankingActivity : AppCompatActivity() {
    private var binding: ActivityRankingBinding? = null
    private var addressList: ArrayList<String?>? = null
    private var dbHelper: DBHelper? = null
    private var count: String? = null
    private var addr: String? = null
    private var rank: String? = null
    private var rankNum = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRankingBinding.inflate(
            layoutInflater
        )
        setContentView(binding!!.root)
        setTitle(R.string.ranking_title)

        // DB접근을 위한 Helper 초기화
        dbHelper = DBHelper(this)
        val sqlDB = dbHelper!!.readableDatabase
        val cursor = sqlDB.rawQuery("select * from addressDB", null)

        // 주소 DB를 받아올 ArrayList 초기화, TextView로 출력할 string 초기화
        rank = "---" + System.lineSeparator()
        count = "----" + System.lineSeparator()
        addr = "-------------------------------------------" + System.lineSeparator()
        addressList = ArrayList()
        if (cursor.moveToFirst()) {
            addressList = dbHelper.allAddressInDB
        }

        // 주소 빈도 수 계산
        val map: MutableMap<String?, Int> = HashMap()
        for (addr in addressList!!) {
            val count = map[addr]
            if (count == null) {
                map[addr] = 1
            } else {
                map[addr] = count + 1
            }
        }

        // 정렬
        // Map.Entry 리스트 작성
        val list_entries: List<Map.Entry<String?, Int>> =
            ArrayList<Map.Entry<String?, Int>>(map.entries)

        // 비교함수 Comparator를 사용하여 오름차순으로 정렬
        Collections.sort(list_entries) { (_, value), (_, value1) ->
            // compare로 값을 비교
            // 내림 차순 정렬
            value1.compareTo(value)
        }

        // 누적하여 String에 추가=
        for ((key, value) in list_entries) {
            rank += rankNum.toString() + System.lineSeparator()
            rank += "---" + System.lineSeparator()
            rankNum++
            count += value.toString() + System.lineSeparator()
            count += "----" + System.lineSeparator()
            addr += key + System.lineSeparator()
            addr += "-------------------------------------------" + System.lineSeparator()
            map[key] = value
        }

        // TextView에 표시
        binding!!.rankingContentRank.text = rank
        binding!!.rankingContentCount.text = count
        binding!!.rankingContentCity.text = addr
    }
}