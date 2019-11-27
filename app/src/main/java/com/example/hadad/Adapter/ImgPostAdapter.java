package com.example.hadad.Adapter;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.hadad.PostDetailActivity;
import com.example.hadad.R;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.net.URL;
import java.util.List;
/**
 * load ảnh từ uri vào từng item cho phần list ảnh của từng post
 */

public class ImgPostAdapter extends PagerAdapter
{
    Context context;
    List<String> imgList;
    String pId;

    public ImgPostAdapter(Context context, List<String> imgList, String pId) {
        this.context = context;
        this.imgList = imgList;
        this.pId = pId;
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

        ImageView imageView = view.findViewById(R.id.img_post);
        TextView txt_count = view.findViewById(R.id.txt_count);
        txt_count.setText(position + 1 + "/" + imgList.size());
        imageView.setBackgroundColor(Color.parseColor("#1b1e44"));
        //Picasso.get().load(imgList.get(position)).networkPolicy(NetworkPolicy.NO_CACHE).memoryPolicy(MemoryPolicy.NO_CACHE).into(imageView);
        Picasso.get().load(imgList.get(position)).into(imageView);
        container.addView(view);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PostDetailActivity.class);
                intent.putExtra("postId", pId);
                context.startActivity(intent);
            }
        });
        return view;
    }
}
