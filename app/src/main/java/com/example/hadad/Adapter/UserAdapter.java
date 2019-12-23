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

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.blogspot.atifsoftwares.circularimageview.CircularImageView;
import com.example.hadad.ChatActivity;
import com.example.hadad.Model.User;

import com.example.hadad.R;
import com.example.hadad.ThereProfileActivity;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.michaelbel.bottomsheet.BottomSheet;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

	Context context;
	List<User> userList;
	FrameLayout frame_users;

	public UserAdapter(Context context, List<User> userList, FrameLayout frame_users) {
		this.context = context;
		this.userList = userList;
		this.frame_users = frame_users;
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

		final String name = user.getName();
		final String email = user.getEmail();
		String avatar = user.getImage();
		final String uid = user.getUid();

		userViewHolder.txt_name.setText(name);
		userViewHolder.txt_email.setText(email);
		try {
			Picasso.get().load(avatar).placeholder(R.drawable.user).networkPolicy(NetworkPolicy.NO_CACHE).memoryPolicy(MemoryPolicy.NO_CACHE).into(userViewHolder.img_avatar);
		}
		 catch (Exception ex)
		 {

		 }

		userViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				String[] nameList = {"Chat", "Profile"};
				int[] iconList = {R.drawable.ic_profile_white, R.drawable.ic_chat_white};
				BottomSheet.Builder builder = new BottomSheet.Builder(context);
				builder.setTitle(name)
						.setItems(nameList, iconList, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
								if(which == 0){
									Intent intent = new Intent(context, ChatActivity.class);
									intent.putExtra("uid", uid);
									context.startActivity(intent);
								} else if (which == 1) {
									Intent intent1 = new Intent(context, ThereProfileActivity.class);
									intent1.putExtra("uid", uid);
									intent1.putExtra("fromUsers", true);
									context.startActivity(intent1);
								}
							}
						})
						.setIconColor(Color.WHITE)
						.setDividers(true)
						.setDarkTheme(true)
						.show();
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
