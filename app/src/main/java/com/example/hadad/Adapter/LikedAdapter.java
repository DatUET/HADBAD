package com.example.hadad.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.blogspot.atifsoftwares.circularimageview.CircularImageView;
import com.example.hadad.Model.User;
import com.example.hadad.R;
import com.example.hadad.ThereProfileActivity;
import com.squareup.picasso.Picasso;

import java.util.List;

public class LikedAdapter extends RecyclerView.Adapter<LikedAdapter.LikedListViewHolder> {
    Context context;
    List<User> userList;

    public LikedAdapter(Context context, List<User> userList) {
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public LikedListViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.row_liked, viewGroup, false);

        LikedListViewHolder likedListViewHolder = new LikedListViewHolder(view);
        return likedListViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull LikedListViewHolder likedListViewHolder, int i) {
        final User user = userList.get(i);
        likedListViewHolder.txt_name.setText(user.getName());
        try {
            Picasso.get().load(user.getImage()).placeholder(R.drawable.user).into(likedListViewHolder.img_avatar);
        }
        catch (Exception ex)
        {

        }
        // Thêm click vào itemview
        likedListViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ThereProfileActivity.class);
                intent.putExtra("uid", user.getUid());
                intent.putExtra("fromHome", true);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public class LikedListViewHolder extends RecyclerView.ViewHolder {
        CircularImageView img_avatar;
        TextView txt_name;
        public LikedListViewHolder(@NonNull View itemView) {
            super(itemView);

            img_avatar = itemView.findViewById(R.id.img_avatar);
            txt_name = itemView.findViewById(R.id.txt_name);
        }
    }
}
