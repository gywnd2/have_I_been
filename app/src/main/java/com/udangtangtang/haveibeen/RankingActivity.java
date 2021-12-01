package com.udangtangtang.haveibeen;

import androidx.appcompat.app.AppCompatActivity;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;

import com.udangtangtang.haveibeen.databinding.ActivityRankingBinding;
import com.udangtangtang.haveibeen.model.DBHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class RankingActivity extends AppCompatActivity {
    private ActivityRankingBinding binding;
    private ArrayList<String> addressList;
    private DBHelper dbHelper;
    private String count, addr, rank;
    private int rankNum=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRankingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setTitle(R.string.ranking_title);

        // DB접근을 위한 Helper 초기화
        dbHelper=new DBHelper(this);
        SQLiteDatabase sqlDB=dbHelper.getReadableDatabase();
        Cursor cursor =sqlDB.rawQuery("select * from addressDB", null);

        // 주소 DB를 받아올 ArrayList 초기화
        rank="---"+System.lineSeparator(); count ="----"+System.lineSeparator(); addr="-------------------------------------------"+System.lineSeparator();
        addressList=new ArrayList<>();
        if(cursor.moveToFirst()){
            addressList= dbHelper.getAllAddressInDB();
        }

        // 주소 빈도 수 계산
        Map<String, Integer> map=new HashMap<String, Integer>();
        for(String addr : addressList){
            Integer count=map.get(addr);
            if(count==null){
                map.put(addr, 1);
            }else{
                map.put(addr, count+1);
            }
        }

        // 정렬
        // Map.Entry 리스트 작성
        List<Entry<String, Integer>> list_entries = new ArrayList<Entry<String, Integer>>(map.entrySet());

        // 비교함수 Comparator를 사용하여 오름차순으로 정렬
        Collections.sort(list_entries, new Comparator<Entry<String, Integer>>() {
            // compare로 값을 비교
            public int compare(Entry<String, Integer> obj1, Entry<String, Integer> obj2) {
                // 내림 차순 정렬
                return obj2.getValue().compareTo(obj1.getValue());
            }
        });

        // 누적하여 String에 추가=
        for(Entry<String, Integer> entry : list_entries){
            rank+=String.valueOf(rankNum)+System.lineSeparator();
            rank+="---"+System.lineSeparator();
            rankNum++;
            count +=entry.getValue()+System.lineSeparator();
            count +="----"+System.lineSeparator();
            addr+=entry.getKey()+System.lineSeparator();
            addr+="-------------------------------------------"+System.lineSeparator();
            map.put(entry.getKey(), entry.getValue());
        }

        binding.rankingContentRank.setText(rank);
        binding.rankingContentCount.setText(count);
        binding.rankingContentCity.setText(addr);

    }
}