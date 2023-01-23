package com.udangtangtang.haveibeen

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.udangtangtang.haveibeen.databinding.ActivitySettingBinding

class SettingActivity : AppCompatActivity() {
    private var binding: ActivitySettingBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        // Title 설정
        setTitle(R.string.setting_title)
        binding!!.settingWebview.loadUrl("https://github.com/gywnd2/have_i_been")

    }

}