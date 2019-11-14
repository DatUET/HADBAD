package com.example.hadad.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.blogspot.atifsoftwares.circularimageview.CircularImageView;
import com.example.hadad.ChatActivity;
import com.example.hadad.Model.User;

import com.example.hadad.R;
import com.example.hadad.ThereProfileActivity;
import com.squareup.picasso.Picasso;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

	Context context;
	List<User> userList;

	public UserAdapter(Context context, List<User> userList) {
		this.context = context;
		this.userList = userList;
	}

	@NonNull
	@Override
	public UserViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.row_user, viewGroup, false);

		UserViewHolder userViewHolder = new UserViewHolder(view);
		return userViewHolder;
	}

	@Override
	public void onBindViewHolder(@NonNull final UserViewHolder userViewHolder, int i) {
		User user = userList.get(i);

		String name = user.getName();
		final String email = user.getEmail();
		String avatar = user.getImage();
		final String uid = user.getUid();

		userViewHolder.txt_name.setText(name);
		userViewHolder.txt_email.setText(email);
		try {
			Picasso.get().load(avatar).placeholder(R.drawable.user).into(userViewHolder.img_avatar);
		}
		 catch (Exception ex)
		 {

		 }

		userViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {


				AlertDialog.Builder builder = new AlertDialog.Builder(context);
				builder.setItems(new String[]{"Profile", "Chat"}, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if(which == 0)
						{
							userViewHolder.img_avatar.setTransitionName("transitionUsers");
							Intent intent = new Intent(context, ThereProfileActivity.class);
							intent.putExtra("uid", uid);
							intent.putExtra("fromUsers", true);
							ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) context, userViewHolder.img_avatar, ViewCompat.getTransitionName(userViewHolder.img_avatar));
							context.startActivity(intent, activityOptionsCompat.toBundle());
						}
						else
						{
							userViewHolder.img_avatar.setTransitionName("transition" + uid);
							Intent intent = new Intent(context, ChatActivity.class);
							intent.putExtra("uid", uid);
							ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) context, userViewHolder.img_avatar, ViewCompat.getTransitionName(userViewHolder.img_avatar));
							context.startActivity(intent, activityOptionsCompat.toBundle());
						}
					}
				});

				AlertDialog alertDialog = builder.create();
				alertDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
				alertDialog.show();
			}
		});
	}

	@Override
	public int getItemCount() {
		return userList.size();
	}

	public class UserViewHolder extends RecyclerView.ViewHolder {
		CircularImageView img_avatar;
		TextView txt_name, txt_email;
		public UserViewHolder(@NonNull View itemView) {
			super(itemView);

			img_avatar = itemView.findViewById(R.id.img_avatar);
			txt_name = itemView.findViewById(R.id.txt_name);
			txt_email = itemView.findViewById(R.id.txt_email);

		}
	}
}
