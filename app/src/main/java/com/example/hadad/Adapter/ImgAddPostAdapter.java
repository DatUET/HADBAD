package com.example.hadad.Adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.hadad.R;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
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
        Uri uri = uriList.get(i);
        if(uri.toString().contains("http"))
            Picasso.get().load(uri).into(imgAddPostViewHolder.img_post);
        else {
            int quality = 100;
            try {
                InputStream is = context.getContentResolver().openInputStream(uri);
                int byte_size = is.available();
                int file_size = byte_size / 1024;
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                if (file_size > 700)
                    quality = (int) (700 / file_size * 100.0);
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
                byte[] data = baos.toByteArray();
                Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                imgAddPostViewHolder.img_post.setImageBitmap(Bitmap.createBitmap(bmp));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
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
