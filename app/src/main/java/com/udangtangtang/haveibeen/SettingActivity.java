package com.udangtangtang.haveibeen;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.udangtangtang.haveibeen.databinding.ActivitySettingBinding;
import com.udangtangtang.haveibeen.model.DBHelper;
import com.udangtangtang.haveibeen.util.PictureScanHelper;

public class SettingActivity extends AppCompatActivity {
    private ActivitySettingBinding binding;
    private DBHelper dbHelper;
    private PictureScanHelper pictureScanHelper;
    private AlertDialog.Builder builder;
    private AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivitySettingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Title 설정
        setTitle("설정");

        pictureScanHelper=new PictureScanHelper(this);
        dbHelper=new DBHelper(getApplicationContext());

        binding.settingButtonInitDb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 확인 창
                builder=new AlertDialog.Builder(SettingActivity.this);
                builder.setTitle(R.string.setting_alert_title_db_init)
                        .setMessage(R.string.setting_alert_message_db_init)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dbHelper.initializeDB();
                                restartApp();
                            }
                        })
                        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                dialog=builder.create();
                builder.show();

            }
        });

        binding.settingButtonRescan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 확인 창
                builder=new AlertDialog.Builder(SettingActivity.this);
                builder.setTitle(R.string.setting_alert_title_rescan)
                        .setMessage(R.string.setting_alert_message_rescan)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                pictureScanHelper.scanPictures(getApplicationContext());
                                restartApp();
                            }
                        })
                        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                dialog=builder.create();
                builder.show();
            }
        });
    }

    // 앱 재시작
    private void restartApp(){
        PackageManager packageManager=getPackageManager();
        Intent intent=packageManager.getLaunchIntentForPackage(getPackageName());
        ComponentName componentName=intent.getComponent();
        Intent mIntent=Intent.makeRestartActivityTask(componentName);
        startActivity(mIntent);
        System.exit(0);
    }
}