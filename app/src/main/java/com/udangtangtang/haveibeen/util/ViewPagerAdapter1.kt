package com.udangtangtang.haveibeen.util

import android.content.*
import android.database.sqlite.SQLiteOpenHelper
import android.database.sqlite.SQLiteDatabase
import com.udangtangtang.haveibeen.model.DBHelper
import com.udangtangtang.haveibeen.model.InfoWindowData
import com.udangtangtang.haveibeen.model.RecordData
import android.location.Geocoder
import android.widget.Toast
import android.os.Build
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.udangtangtang.haveibeen.util.PermissionHelper
import androidx.core.app.ActivityCompat
import com.udangtangtang.haveibeen.MainActivity
import androidx.recyclerview.widget.RecyclerView
import com.udangtangtang.haveibeen.util.ViewPagerAdapter.ViewHolderPage
import android.view.ViewGroup
import android.view.LayoutInflater
import com.udangtangtang.haveibeen.R
import com.bumptech.glide.Glide
import com.udangtangtang.haveibeen.util.GeocodingHelper
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import com.udangtangtang.haveibeen.util.ViewPagerAdapter
import androidx.viewpager2.widget.ViewPager2
import com.udangtangtang.haveibeen.util.PictureScanHelper
import java.io.File
import java.util.ArrayList

class ViewPagerAdapter(private val context: Context, latLng: Array<String?>) :
    RecyclerView.Adapter<ViewHolderPage>() {
    private var sameLocationPictures: ArrayList<String?>?
    private val dbHelper: DBHelper

    init {
        dbHelper = DBHelper(context)
        sameLocationPictures = ArrayList()

        // 입력 받은 위/경도로 같은 위치 이미지 모두 가져오기
        sameLocationPictures.clear()
        sameLocationPictures = dbHelper.getSameLocationPictures(latLng)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderPage {
        val context = parent.context
        val view =
            LayoutInflater.from(context).inflate(R.layout.viewpager2_record_detail, parent, false)
        return ViewHolderPage(view)
    }

    override fun onBindViewHolder(holder: ViewHolderPage, position: Int) {
        holder.onBind(sameLocationPictures!![position])
    }

    override fun getItemCount(): Int {
        return sameLocationPictures!!.size
    }

    fun clearSameLocationPicturesList() {
        sameLocationPictures!!.clear()
    }

    inner class ViewHolderPage internal constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView

        init {
            imageView = itemView.findViewById(R.id.viewpager2_record_detail_image)
        }

        fun onBind(filename: String?) {
            // 사진 파일 전체 경로로 이미지 설정
            Glide.with(context)
                .load(File(filename))
                .into(imageView)
        }
    }
}