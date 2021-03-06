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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import cn.pedant.SweetAlert.SweetAlertDialog;

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
			Picasso.get().load(uDp).placeholder(R.drawable.user).networkPolicy(NetworkPolicy.NO_CACHE).memoryPolicy(MemoryPolicy.NO_CACHE).into(commentViewHolder.img_avatar); // tải ảnh của người cmt vào ImageView
		}
		catch (Exception ex)
		{

		}

		commentViewHolder.txt_name.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(context, ThereProfileActivity.class);
				intent.putExtra("uid", uid);
				context.startActivity(intent);
			}
		});

		// nếu người dùng bấm vào item nào và id hiện tại đc lưu phải tùng với id của tk cmt thì show thông báo hỏi muốn xóa hay ko
		commentViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(uid.equals(myUid)) {
					new SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
							.setTitleText("Are you sure?")
							.setContentText("You won't be able to recover this message!")
							.setConfirmButton("Delete", new SweetAlertDialog.OnSweetClickListener() {
								@Override
								public void onClick(SweetAlertDialog sweetAlertDialog) {
									deleteComment(cId);
									sweetAlertDialog.dismiss();
								}
							})
							.setCancelButton("No", new SweetAlertDialog.OnSweetClickListener() {
								@Override
								public void onClick(SweetAlertDialog sweetAlertDialog) {
									sweetAlertDialog.dismiss();
								}
							})
							.show();

				}
			}
		});
	}

	private void deleteComment(String cId) {
		final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Comments").child(postId);
		reference.child(cId).removeValue()
				.addOnCompleteListener(new OnCompleteListener<Void>() {
					@Override
					public void onComplete(@NonNull Task<Void> task) {
						new SweetAlertDialog(context, SweetAlertDialog.SUCCESS_TYPE)
								.setTitleText("Deleted!")
								.show();
					}
				})
				.addOnFailureListener(new OnFailureListener() {
					@Override
					public void onFailure(@NonNull Exception e) {
						new SweetAlertDialog(context, SweetAlertDialog.SUCCESS_TYPE)
								.setTitleText("Sorry!")
								.setContentText(e.getMessage())
								.show();
					}
				});

		// sau khi xóa cập nhật lại số lượng đã cmt
//		reference.addListenerForSingleValueEvent(new ValueEventListener() {
//			@Override
//			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//				final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Post").child(postId);
//				ref.addListenerForSingleValueEvent(new ValueEventListener() {
//					@Override
//					public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//						String comments = dataSnapshot.child("pComments").getValue() + "";
//						int newCommentCount = Integer.parseInt(comments) - 1;
//						ref.child("pComments").setValue(newCommentCount + "");
//					}
//
//					@Override
//					public void onCancelled(@NonNull DatabaseError databaseError) {
//
//					}
//				});
//
//			}
//
//			@Override
//			public void onCancelled(@NonNull DatabaseError databaseError) {
//
//			}
//		});
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
