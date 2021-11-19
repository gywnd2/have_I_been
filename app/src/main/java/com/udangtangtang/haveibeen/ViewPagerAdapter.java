package com.udangtangtang.haveibeen;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;

public class ViewPagerAdapter extends RecyclerView.Adapter<ViewPagerAdapter.ViewHolderPage> {
    private Context context;
    private ArrayList<String> sameLocationPictures;
    private DBHelper dbHelper;
    private String latitude, longtitude;

    ViewPagerAdapter(Context context, String latitude, String longtitude) {
        this.context = context;
        dbHelper = new DBHelper(context);
        this.sameLocationPictures = new ArrayList<>();
        this.latitude=latitude;
        this.longtitude=longtitude;

        // 입력 받은 위/경도로 같은 위치 이미지 가져오기
        this.sameLocationPictures.clear();
        this.sameLocationPictures = dbHelper.getSameLocationPictures(latitude, longtitude);
    }

    @NonNull
    @Override
    public ViewHolderPage onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.viewpager2_record_detail, parent, false);

        return new ViewHolderPage(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderPage holder, int position) {
        holder.onBind(sameLocationPictures.get(position));
    }

    @Override
    public int getItemCount() {
        return sameLocationPictures.size();
    }

    public void clearSameLocationPicturesList(){
        this.sameLocationPictures.clear();
    }

    public class ViewHolderPage extends RecyclerView.ViewHolder {
        private ImageView imageView;

        ViewHolderPage(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.viewpager2_record_detail_image);
        }

        public void onBind(String filename) {
            // 사진 파일 전체 경로로 이미지 설정
            Glide.with(context)
                    .load(new File(filename))
                    .into(imageView);
        }
    }
}
