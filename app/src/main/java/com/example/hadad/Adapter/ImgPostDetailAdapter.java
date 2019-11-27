package com.example.hadad.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.hadad.ImagePostActivity;
import com.example.hadad.PostDetailActivity;
import com.example.hadad.R;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.List;
/**
 * load ảnh từ uri vào từng item cho phần list ảnh của từng post trong màn hình post details
 */

public class ImgPostDetailAdapter extends PagerAdapter
{
    Context context;
    List<String> imgList;

    public ImgPostDetailAdapter(Context context, List<String> imgList) {
        this.context = context;
        this.imgList = imgList;
    }

    @Override
    public int getCount() {
        return imgList.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull final ViewGroup container, final int position) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.col_img_post, container, false);

        final ImageView imageView = view.findViewById(R.id.img_post);
        TextView txt_count = view.findViewById(R.id.txt_count);
        txt_count.setText(position + 1 + "/" + imgList.size());
        imageView.setBackgroundColor(Color.parseColor("#1b1e44"));
        Picasso.get().load(imgList.get(position)).networkPolicy(NetworkPolicy.NO_CACHE).memoryPolicy(MemoryPolicy.NO_CACHE).into(imageView);
        //Picasso.get().load(imgList.get(position)).into(imageView);
        container.addView(view);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ImagePostActivity.class);
                intent.putExtra("pImage", imgList.get(position));
                ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation((Activity)context, imageView, ViewCompat.getTransitionName(imageView));
                context.startActivity(intent, activityOptionsCompat.toBundle());
            }
        });
        return view;
    }
}


