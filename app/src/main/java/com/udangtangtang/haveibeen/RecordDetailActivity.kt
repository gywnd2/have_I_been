package com.udangtangtang.haveibeen

import android.app.AlertDialog
import android.database.sqlite.SQLiteOpenHelper
import android.database.sqlite.SQLiteDatabase
import com.udangtangtang.haveibeen.model.DBHelper
import android.content.ContentValues
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
import android.content.Intent
import com.udangtangtang.haveibeen.util.ViewPagerAdapter
import androidx.viewpager2.widget.ViewPager2
import android.content.DialogInterface
import com.udangtangtang.haveibeen.util.PictureScanHelper
import android.content.ComponentName
import android.location.Address
import android.media.ExifInterface
import android.view.View
import com.udangtangtang.haveibeen.databinding.ActivityRecordDetailBinding
import java.io.IOException

class RecordDetailActivity : AppCompatActivity() {
    private var binding: ActivityRecordDetailBinding? = null
    private var dbHelper: DBHelper? = null
    private var selectedLatLng: Array<String?>
    private var exifInterface: ExifInterface? = null
    private var geocodingHelper: GeocodingHelper? = null
    private var firstFileName: String? = null
    private var dialog: AlertDialog? = null
    private val addressList: List<Address>? = null
    private var builder: AlertDialog.Builder? = null
    private var recordData: RecordData? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecordDetailBinding.inflate(
            layoutInflater
        )
        setContentView(binding!!.root)

        // MainActivity로 부터 fileName 받아오기
        val intent = intent
        selectedLatLng = arrayOfNulls(2)
        selectedLatLng = intent.getStringArrayExtra("selectedLatLng")!!

        // 전달받은 위/경도 정보를 ViewPager 어댑터로 전달
        // 같은 위/경도에 해당하는 모든 사진을 ViewPager에 추가
        binding!!.recordDetailViewpager2.adapter = ViewPagerAdapter(this, selectedLatLng)
        // 가로 스크롤 설정
        binding!!.recordDetailViewpager2.orientation = ViewPager2.ORIENTATION_HORIZONTAL

        // DB연결을 위해 Helper 초기화
        dbHelper = DBHelper(this)

        // 같은 위/경도를 갖는 이미지 중 맨 처음 파일에 데이터 저장
        firstFileName = dbHelper!!.getFileNameByLatLng(selectedLatLng)

        // 데이터 가져오기
        recordData = dbHelper!!.getRecordData(firstFileName)

        // 액티비티 타이틀 설정
        title =
            if (recordData.getLocationName() == null) getString(R.string.record_detail_no_locName) else recordData.getLocationName()

        // Indicator 설정
        binding!!.recordDetailImageIndicator.setViewPager(binding!!.recordDetailViewpager2)
        binding!!.recordDetailImageIndicator.createIndicators(
            dbHelper!!.getSameLocationPictures(
                selectedLatLng
            ).size, 0
        )

        // 시간, 날짜 정보를 가져오기 위한 exifInterface 초기화
        try {
            exifInterface = ExifInterface(firstFileName!!)
        } catch (e: IOException) {
        }
        // 지오코딩을 위한 Helper 초기화
        geocodingHelper = GeocodingHelper(
            this, java.lang.Double.valueOf(selectedLatLng[0]), java.lang.Double.valueOf(
                selectedLatLng[1]
            )
        )

        // 정보 표시
        binding!!.recordDetailAddress.text = geocodingHelper!!.address
        binding!!.recordDetailLocationName.text =
            if (recordData.getLocationName() == null) getString(R.string.record_detail_no_locName) else recordData.getLocationName()
        binding!!.recordDetailDatetime.text =
            exifInterface!!.getAttribute(ExifInterface.TAG_DATETIME)
        binding!!.recordDetailRating.rating =
            if (recordData.getRating().toDouble() == 0.0) 0.0.toFloat() else recordData.getRating()
        binding!!.recordDetailComment.text =
            if (recordData.getComment() == null) getString(R.string.record_detail_no_comment) else recordData.getComment()

        // 수정용 View 가리기
        binding!!.recordDetailEditLocationName.visibility = View.INVISIBLE
        binding!!.recordDetailEditRating.visibility = View.INVISIBLE
        binding!!.recordDetailEditComment.visibility = View.INVISIBLE
        binding!!.recordDetailEditButtonSave.visibility = View.INVISIBLE

        // 수정 클릭시 EditText로 모두 전환
        binding!!.recordDetailButtonEdit.setOnClickListener { // 보기용 View는 가리기
            binding!!.recordDetailLocationName.visibility = View.INVISIBLE
            binding!!.recordDetailRating.visibility = View.INVISIBLE
            binding!!.recordDetailComment.visibility = View.INVISIBLE
            binding!!.recordDetailButtonEdit.visibility = View.INVISIBLE

            // 수정용 View와 보기용 View의 내용 일치시키기
            binding!!.recordDetailEditLocationName.setText(binding!!.recordDetailLocationName.text)
            binding!!.recordDetailEditRating.rating = binding!!.recordDetailRating.rating
            binding!!.recordDetailEditComment.setText(binding!!.recordDetailComment.text)

            // 수정용 View 표시
            binding!!.recordDetailEditLocationName.visibility = View.VISIBLE
            binding!!.recordDetailEditRating.visibility = View.VISIBLE
            binding!!.recordDetailEditComment.visibility = View.VISIBLE
            binding!!.recordDetailEditButtonSave.visibility = View.VISIBLE
        }

        // 저장 클릭시 TextView로 모두 전환
        binding!!.recordDetailEditButtonSave.setOnClickListener { // 저장 확인 창
            builder = AlertDialog.Builder(this@RecordDetailActivity)
            builder!!.setTitle(R.string.record_detail_title_save)
                .setMessage(R.string.record_detail_message_save)
                .setPositiveButton(R.string.yes) { dialog, which -> // DB 업데이트
                    dbHelper!!.updateDB(
                        firstFileName,
                        binding!!.recordDetailEditLocationName.text.toString(),
                        binding!!.recordDetailEditRating.rating,
                        binding!!.recordDetailEditComment.text.toString()
                    )

                    // 저장 되었음을 확인하기 위해 저장 시에는 다시 조회하여 출력
                    // 받아온 fileName으로 기록 조회
                    recordData = dbHelper!!.getRecordData(firstFileName)
                    // 데이터 내용 표시
                    binding!!.recordDetailLocationName.text =
                        if (recordData.getLocationName() == null) "" else recordData.getLocationName()
                    binding!!.recordDetailAddress.text = geocodingHelper!!.address
                    binding!!.recordDetailDatetime.text =
                        exifInterface!!.getAttribute(ExifInterface.TAG_DATETIME)
                    binding!!.recordDetailRating.rating = if (recordData.getRating()
                            .toDouble() == 0.0
                    ) 0.0.toFloat() else recordData.getRating()
                    binding!!.recordDetailComment.text =
                        if (recordData.getComment() == null) "" else recordData.getComment()

                    // 수정용 View 가리기
                    binding!!.recordDetailEditLocationName.visibility = View.INVISIBLE
                    binding!!.recordDetailEditRating.visibility = View.INVISIBLE
                    binding!!.recordDetailEditComment.visibility = View.INVISIBLE
                    binding!!.recordDetailEditButtonSave.visibility = View.INVISIBLE

                    // 보기용 View 표시
                    binding!!.recordDetailLocationName.visibility = View.VISIBLE
                    binding!!.recordDetailRating.visibility = View.VISIBLE
                    binding!!.recordDetailComment.visibility = View.VISIBLE
                    binding!!.recordDetailButtonEdit.visibility = View.VISIBLE
                    Toast.makeText(applicationContext, getString(R.string.saved), Toast.LENGTH_LONG)
                        .show()
                }
                .setNegativeButton(R.string.no) { dialog, which -> }

            // 확인창 표시
            dialog = builder!!.create()
            builder!!.show()
        }
    }
}