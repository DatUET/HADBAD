package com.example.hadad.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.blogspot.atifsoftwares.circularimageview.CircularImageView;
import com.example.hadad.ImagePostActivity;
import com.example.hadad.Model.Chat;
import com.example.hadad.R;

import com.example.hadad.VideoViewActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * thiết kế cho từng item chat
 */
public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {
	private static final int MSG_TYPE_LEFT = 0;
	private static final int MSG_TYPE_RIGHT = 1;

	Context context;
	List<Chat> chatList;
	String imgUrl;

	FirebaseUser firebaseUser;

	public ChatAdapter(Context context, List<Chat> chatList, String imgUrl) {
		this.context = context;
		this.chatList = chatList;
		this.imgUrl = imgUrl;
	}

	// build giao diện cho item, nếu là i=0 (MSG_TYPE_LEFT) thì build file row_chat_left.xml
	// nếu i=1 (MSG_TYPE_RIGHT) thì build file row_chat_right.xml
	// còn i là giá trị trả về của hàm getItemCount()
	@NonNull
	@Override
	public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		if(i == MSG_TYPE_LEFT) {
			View view = inflater.inflate(R.layout.row_chat_left, viewGroup, false);
			ChatViewHolder chatViewHolder = new ChatViewHolder(view);
			return chatViewHolder;
		}
		else
		{
			View view = inflater.inflate(R.layout.row_chat_right, viewGroup, false);
			ChatViewHolder chatViewHolder = new ChatViewHolder(view);
			return chatViewHolder;
		}
	}

	// hàm onBindViewHolder đưa dữ liệu vào cho từng item, và xử lý sự kiện cho mỗi thành phần trong item đó
	@Override
	public void onBindViewHolder(@NonNull final ChatViewHolder chatViewHolder, final int i) {
		final Chat chat = chatList.get(i);
		String message = chat.getMessage();
		String timeStamp = chat.getTimestamp();
		// chuyển thời gian về dạng dd/MM//yyyy hh:mm aa
		Calendar cal = Calendar.getInstance(Locale.ENGLISH);
		cal.setTimeInMillis(Long.parseLong(timeStamp));
		String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa", cal).toString();

		// 1 tin nhắn có thể có 3 phần chính: tin nhắn(dạng văn bản), ảnh, video
		if (!message.equals("")) {
			// nếu tin nhắt có nội dung văn bản thì hiển thị phần TextView để chứa nội dung lên và đưa nội dung vào đấy
			chatViewHolder.txt_message.setVisibility(View.VISIBLE);
			chatViewHolder.txt_message.setText(message);
		}
		else
		{
			// ngược lại nếu ko có thì ẩn n đi
			chatViewHolder.txt_message.setVisibility(View.GONE);
		}
		chatViewHolder.txt_time.setText(dateTime); // đưa thời gian đã gửi của tin nhắn vào TextView
		try {
			Picasso.get().load(imgUrl).into(chatViewHolder.img_avatar); // tải avater của người gửi vào ImageView
		}
		catch (Exception ex)
		{

		}
		if (!chat.getImage().equals("noImage"))
		{
			// nếu tin nhắn có ảnh thì load ảnh vào ImageView của item tin nhắn đó
			chatViewHolder.img_chat.setVisibility(View.VISIBLE);
			try {
				Picasso.get().load(chat.getImage()).into(chatViewHolder.img_chat);
			}
			catch (Exception ex)
			{

			}
		}
		else
		{
			// nếu ko có thì ẩn đi
			chatViewHolder.img_chat.setVisibility(View.GONE);
		}
		if (!chat.getVideo().equals("noVideo"))
		{
			// tương tự cho video
			chatViewHolder.video_chat.setVisibility(View.VISIBLE);
			Uri uri = Uri.parse(chat.getVideo());
			chatViewHolder.video_chat.setVideoURI(uri);
			chatViewHolder.video_chat.requestFocus();
			chatViewHolder.video_chat.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
				@Override
				public void onPrepared(MediaPlayer mp) {
					mp.setLooping(true);
					chatViewHolder.video_chat.start();
				}
			});

		}
		else
		{
			chatViewHolder.video_chat.setVisibility(View.GONE);
		}
		// khi người dùng chạm vào phần TextView của tin nhắn thì xuất thông báo muốn xóa tin nhắn đó hay ko?
		chatViewHolder.layout_message.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				AlertDialog.Builder builder = new AlertDialog.Builder(context);
				builder.setTitle("Delete");
				builder.setMessage("Are you sure to delete this message?");

				builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						deleteMessage(i);
					}
				});

				builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
				AlertDialog alertDialog = builder.create();
				alertDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation; // tạo animation cho thông báo chạy từ dưới lên
				alertDialog.show();
			}
		});
		if(i == chatList.size() -1)
		{
			if(chat.isIsseen())
			{
				chatViewHolder.txt_seen.setText("Seen");
			}
			else
			{
				chatViewHolder.txt_seen.setText("Delivered");
			}
		}
		else
		{
			chatViewHolder.txt_seen.setVisibility(View.GONE);
		}
		// bấm vào ảnh thì chuyển qua Activity xem ảnh
		chatViewHolder.img_chat.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(context, ImagePostActivity.class);
				intent.putExtra("pImage", chat.getImage());
				ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation((Activity)context, chatViewHolder.img_chat, ViewCompat.getTransitionName(chatViewHolder.img_chat));
				context.startActivity(intent, activityOptionsCompat.toBundle());
			}
		});
		// bấm vào ảnh thì chuyển qua Activity xem video
		chatViewHolder.video_chat.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(context, VideoViewActivity.class);
				intent.putExtra("url", chat.getVideo());
				context.startActivity(intent);
			}
		});
	}

	@Override
	public int getItemCount() {
		return chatList.size();
	}

	// tìm các thành phần bên file layout
	public class ChatViewHolder extends RecyclerView.ViewHolder {
		CircularImageView img_avatar;
		TextView txt_time, txt_message, txt_seen;
		LinearLayout layout_message;
		ImageView img_chat;
		VideoView video_chat;

		public ChatViewHolder(@NonNull View itemView) {
			super(itemView);

			img_avatar = itemView.findViewById(R.id.img_avatar);
			txt_message = itemView.findViewById(R.id.txt_message);
			txt_time = itemView.findViewById(R.id.txt_time);
			txt_seen = itemView.findViewById(R.id.txt_seen);
			layout_message = itemView.findViewById(R.id.layout_message);
			img_chat = itemView.findViewById(R.id.img_chat);
			video_chat = itemView.findViewById(R.id.video_chat);
		}
	}

	@Override
	public int getItemViewType(int position) {
		firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
		if(chatList.get(position).getSender().equals(firebaseUser.getUid()))
		{
			return MSG_TYPE_RIGHT;
		}
		else
		{
			return MSG_TYPE_LEFT;
		}
	}

	private void deleteMessage(int position) {

		final String myuid = FirebaseAuth.getInstance().getCurrentUser().getUid(); // lấy Id của người dùng đã được lưu trong app
		String timestamp = chatList.get(position).getTimestamp();
		DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");
		Query query = reference.orderByChild("timestamp").equalTo(timestamp);
		query.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				for(DataSnapshot snapshot : dataSnapshot.getChildren())
				{
					// nếu tk gửi là ng dùng thì mới xóa đc
					if (snapshot.child("sender").getValue().equals(myuid)) {
						// cách 1: xóa luôn data
						//snapshot.getRef().removeValue();

						//cách 2: thay đổi data: nên chọn
						HashMap<String, Object> hashMap = new HashMap<>();
						hashMap.put("message", "This message was deleted...You can't see");
						hashMap.put("image", "noImage");
						hashMap.put("video", "noVideo");
						if (!snapshot.child("image").getValue().toString().equals("noImage")) {
							// nếu có ảnh thì xóa đi, đỡ chật storage
							StorageReference picRef = FirebaseStorage.getInstance().getReferenceFromUrl(snapshot.child("image").getValue() + "");
							picRef.delete();
						}
						if (!snapshot.child("video").getValue().toString().equals("noVideo")) {
							// nếu có video thì xóa đi
							StorageReference videoRef = FirebaseStorage.getInstance().getReferenceFromUrl(snapshot.child("video").getValue() + "");
							videoRef.delete();
						}

						snapshot.getRef().updateChildren(hashMap);

						Toast.makeText(context, "Message deleted...", Toast.LENGTH_LONG).show();
					}
					else
					{
						Toast.makeText(context, "You can delete only your messages.", Toast.LENGTH_LONG).show();
					}
				}
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {

			}
		});
	}
}
