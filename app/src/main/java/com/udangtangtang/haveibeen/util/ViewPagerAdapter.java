package com.udangtangtang.haveibeen.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.udangtangtang.haveibeen.R;
import com.udangtangtang.haveibeen.model.DBHelper;

import java.io.File;
import java.util.ArrayList;

public class ViewPagerAdapter extends RecyclerView.Adapter<ViewPagerAdapter.ViewHolderPage> {
    private Context context;
    private ArrayList<String> sameLocationPictures;
    private DBHelper dbHelper;

    public ViewPagerAdapter(Context context, String[] latLng) {
        this.context = context;
        dbHelper = new DBHelper(context);
        this.sameLocationPictures = new ArrayList<>();

        // 입력 받은 위/경도로 같은 위치 이미지 가져오기
        this.sameLocationPictures.clear();
        this.sameLocationPictures = dbHelper.getSameLocationPictures(latLng);
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
