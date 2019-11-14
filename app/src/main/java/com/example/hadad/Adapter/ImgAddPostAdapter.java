package com.example.hadad.Adapter;

import android.content.Context;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.hadad.R;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * load ảnh từ uri vào từng item cho phần list ảnh trong màn hình add post
 */
public class ImgAddPostAdapter extends RecyclerView.Adapter<ImgAddPostAdapter.ImgAddPostViewHolder> {

    Context context;
    List<Uri> uriList;

    public ImgAddPostAdapter(Context context, List<Uri> uriList) {
        this.context = context;
        this.uriList = uriList;
    }

    @NonNull
    @Override
    public ImgAddPostViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.col_img_add_post, viewGroup, false);

        ImgAddPostViewHolder imgAddPostViewHolder = new ImgAddPostViewHolder(view);
        return imgAddPostViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ImgAddPostViewHolder imgAddPostViewHolder, final int i) {
        Picasso.get().load(uriList.get(i)).into(imgAddPostViewHolder.img_post);
        imgAddPostViewHolder.img_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uriList.remove(i);
                notifyDataSetChanged();
            }
        });
    }


    @Override
    public int getItemCount() {
        return uriList.size();
    }

    public class ImgAddPostViewHolder extends RecyclerView.ViewHolder {
        ImageView img_post, img_delete;
        public ImgAddPostViewHolder(@NonNull View itemView) {
            super(itemView);

            img_post = itemView.findViewById(R.id.img_post);
            img_delete = itemView.findViewById(R.id.img_delete);
        }
    }
}
