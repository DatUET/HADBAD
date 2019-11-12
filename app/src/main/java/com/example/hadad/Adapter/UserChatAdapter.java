package com.example.hadad.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.blogspot.atifsoftwares.circularimageview.CircularImageView;
import com.example.hadad.ChatActivity;
import com.example.hadad.Model.Chat;
import com.example.hadad.Model.User;
import com.example.hadad.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

public class UserChatAdapter extends RecyclerView.Adapter<UserChatAdapter.UserChatViewHolder> {

	Context context;
	List<User> userList;

	private DatabaseReference reference;
	String myuid;
	Chat lastChat;

	public UserChatAdapter(Context context, List<User> userList) {
		this.context = context;
		this.userList = userList;
		if (FirebaseAuth.getInstance().getCurrentUser() != null) {
			myuid = FirebaseAuth.getInstance().getCurrentUser().getUid();
			reference = FirebaseDatabase.getInstance().getReference("Chats");
			lastChat = new Chat();
		}
	}

	@NonNull
	@Override
	public UserChatViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.row_user_chat, viewGroup, false);

		UserChatViewHolder userChatViewHolder = new UserChatViewHolder(view);
		return userChatViewHolder;
	}

	@Override
	public void onBindViewHolder(@NonNull final UserChatViewHolder userChatViewHolder, int i) {
		User user = userList.get(i);

		String name = user.getName();
		final String email = user.getEmail();
		String avatar = user.getImage();
		final String uid = user.getUid();

		userChatViewHolder.txt_name.setText(name);
		userChatViewHolder.txt_email.setText(email);
		userChatViewHolder.img_avatar.setTransitionName("transition" + uid);
		try {
			Picasso.get().load(avatar).placeholder(R.drawable.user).into(userChatViewHolder.img_avatar);
		}
		catch (Exception ex)
		{

		}

		userChatViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				Intent intent = new Intent(context, ChatActivity.class);
				intent.putExtra("uid", uid);
				ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation((Activity)context, userChatViewHolder.img_avatar, ViewCompat.getTransitionName(userChatViewHolder.img_avatar));
				context.startActivity(intent, activityOptionsCompat.toBundle());
			}
		});



		readLastMsg(uid, userChatViewHolder.txt_last_msg, userChatViewHolder.txt_check_seen, userChatViewHolder.img_dot_new_msg);
		setOnline(uid, userChatViewHolder.img_online);
	}
	// check ng dùng có online hay ko
	private void setOnline(String uid, final ImageView img_online) {
		DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(uid).child("onlineStatus");
		databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				String online = dataSnapshot.getValue(String.class);
				if(online.equals("online"))
				{
					img_online.setImageResource(R.drawable.dot_online);
				}
				else
				{
					img_online.setImageResource(R.drawable.dot_offline);
				}
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {

			}
		});
	}

	// lấy tin nhắn cuối cùng
	private void readLastMsg(final String uid, final TextView txt_last_msg, final TextView txt_check_seen, final CircularImageView img_dot_new_msg) {
		if(reference != null) {
			reference.addValueEventListener(new ValueEventListener() {
				@Override
				public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
					for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
						Chat chat = snapshot.getValue(Chat.class);
						if ((chat.getReciver().equals(myuid) && chat.getSender().equals(uid)) ||
								chat.getReciver().equals(uid) && chat.getSender().equals(myuid)) {
							lastChat = chat;
						}
					}
					if(!lastChat.getImage().equals("noImage"))
					{
						txt_last_msg.setText("This is a image");
					}
					else if(!lastChat.getVideo().equals("noVideo"))
					{
						txt_last_msg.setText("This is a video");
					}
					else {
						txt_last_msg.setText(lastChat.getMessage());
					}
					if (lastChat.getSender().equals(myuid) && lastChat.isIsseen()) {

						txt_check_seen.setVisibility(View.VISIBLE);
					} else {
						img_dot_new_msg.setVisibility(View.GONE);
						txt_check_seen.setVisibility(View.GONE);
					}
					if (!lastChat.getSender().equals(myuid) && !lastChat.isIsseen()) {
						img_dot_new_msg.setVisibility(View.VISIBLE);
						txt_last_msg.setTypeface(Typeface.DEFAULT_BOLD);
					} else {
						txt_last_msg.setTypeface(Typeface.DEFAULT);
					}
				}

				@Override
				public void onCancelled(@NonNull DatabaseError databaseError) {

				}
			});
		}
	}

	@Override
	public int getItemCount() {
		return userList.size();
	}

	public class UserChatViewHolder extends RecyclerView.ViewHolder {

		CircularImageView img_avatar, img_dot_new_msg;
		ImageView img_online;
		TextView txt_name, txt_email, txt_last_msg, txt_check_seen;

		public UserChatViewHolder(@NonNull View itemView) {
			super(itemView);

			img_avatar = itemView.findViewById(R.id.img_avatar);
			txt_name = itemView.findViewById(R.id.txt_name);
			txt_email = itemView.findViewById(R.id.txt_email);
			txt_last_msg = itemView.findViewById(R.id.txt_last_msg);
			txt_check_seen = itemView.findViewById(R.id.txt_check_seen);
			img_dot_new_msg = itemView.findViewById(R.id.img_dot_new_msg);
			img_online = itemView.findViewById(R.id.img_online);
		}
	}
}
