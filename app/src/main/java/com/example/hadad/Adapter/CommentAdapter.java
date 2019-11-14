package com.example.hadad.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.blogspot.atifsoftwares.circularimageview.CircularImageView;
import com.example.hadad.Model.Comment;
import com.example.hadad.R;
import com.example.hadad.ThereProfileActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

	Context context;
	List<Comment> commentList;
	String myUid, postId;

	public CommentAdapter(Context context, List<Comment> commentList, String myUid, String postId) {
		this.context = context;
		this.commentList = commentList;
		this.myUid = myUid;
		this.postId = postId;
	}

	public CommentAdapter() {
	}

	// build file layout cho từng item chat
	@NonNull
	@Override
	public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.row_comment, viewGroup, false);

		CommentViewHolder commentViewHolder = new CommentViewHolder(view);
		return commentViewHolder;
	}

	// đưa dữ liệu và xử lý sự kiện cho từng thành phần trong item
	@Override
	public void onBindViewHolder(@NonNull final CommentViewHolder commentViewHolder, int i) {
		Comment comment = commentList.get(i);

		final String cId = comment.getcId();
		String contentComment =comment.getComment();
		String timestamp = comment.getTimestamp();
		final String uid = comment.getuId();
		String uEmail = comment.getuEmail();
		String uDp = comment.getuDp();
		String uName = comment.getuName();

		// lấy thời gian của hệ thống
		Calendar cal = Calendar.getInstance(Locale.ENGLISH);
		cal.setTimeInMillis(Long.parseLong(timestamp));
		String cTime = DateFormat.format("dd/MM/yyyy hh:mm aa", cal).toString();

		commentViewHolder.txt_name.setText(uName);
		commentViewHolder.txt_comment.setText(contentComment);
		commentViewHolder.txt_time.setText(cTime);

		try {
<<<<<<< HEAD
			Picasso.get().load(uDp).placeholder(R.drawable.user).into(commentViewHolder.img_avatar);
=======
			Picasso.get().load(uDp).placeholder(R.drawable.user).into(commentViewHolder.img_avatar); // tải ảnh của người cmt vào ImageView
>>>>>>> 6c70d8ed964feb082872ba35e70c6ffa52556402
		}
		catch (Exception ex)
		{

		}

		commentViewHolder.txt_name.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(context, ThereProfileActivity.class);
				intent.putExtra("uid", uid);
				ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation((Activity)context, commentViewHolder.img_avatar, ViewCompat.getTransitionName(commentViewHolder.img_avatar)); // hiệu ứng chuyển activity
				context.startActivity(intent, activityOptionsCompat.toBundle());
			}
		});

		// nếu người dùng bấm vào item nào và id hiện tại đc lưu phải tùng với id của tk cmt thì show thông báo hỏi muốn xóa hay ko
		commentViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(uid.equals(myUid)) {
					AlertDialog.Builder builder = new AlertDialog.Builder(context);
					builder.setTitle("Delte Comment");
					builder.setMessage("Are you sure to delete this comment?");
					builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							deleteComment(cId);
						}
					})
							.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									dialog.dismiss();
								}
							});
					AlertDialog alertDialog = builder.create();
					alertDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
					alertDialog.show();
				}
			}
		});
	}

	private void deleteComment(String cId) {
		final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Post").child(postId);
		reference.child("Comments").child(cId).removeValue();

		// sau khi xóa cập nhật lại số lượng đã cmt
		reference.addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				String comments = dataSnapshot.child("pComments").getValue() + "";
				int newCommentCount = Integer.parseInt(comments) - 1;
				reference.child("pComments").setValue(newCommentCount + "");
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {

			}
		});
	}

	@Override
	public int getItemCount() {
		return commentList.size();
	}

	public class CommentViewHolder extends RecyclerView.ViewHolder {
		CircularImageView img_avatar;
		TextView txt_name, txt_comment, txt_time;
		public CommentViewHolder(@NonNull View itemView) {
			super(itemView);

			img_avatar = itemView.findViewById(R.id.img_avatar);
			txt_name = itemView.findViewById(R.id.txt_name);
			txt_comment = itemView.findViewById(R.id.txt_comment);
			txt_time = itemView.findViewById(R.id.txt_time);
		}
	}
}
