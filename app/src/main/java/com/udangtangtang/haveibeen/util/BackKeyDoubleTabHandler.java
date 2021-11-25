package com.udangtangtang.haveibeen.util;

import android.app.Activity;
import android.widget.Toast;

public class BackKeyDoubleTabHandler {
    private Activity activity;
    private Toast toast;
    private long backKeyTime=0;

    public BackKeyDoubleTabHandler(Activity activity){
        this.activity=activity;
    }

    private void notice(){
        toast=Toast.makeText(activity, "뒤로가기를 한번 더 누르면 종료됩니다.", Toast.LENGTH_LONG);
        toast.show();
    }

    public void onBackPressed() {
        if(System.currentTimeMillis()>backKeyTime+2000){
            backKeyTime=System.currentTimeMillis();
            notice();
            return;
        }

        if(System.currentTimeMillis()<=backKeyTime+2000){
            activity.finish();
            toast.show();
        }
    }
}
