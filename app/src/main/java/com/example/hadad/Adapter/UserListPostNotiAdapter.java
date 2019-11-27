package com.example.hadad.Adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.blogspot.atifsoftwares.circularimageview.CircularImageView;
import com.example.hadad.Model.User;
import com.example.hadad.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.List;

public class UserListPostNotiAdapter extends RecyclerView.Adapter<UserListPostNotiAdapter.UserListPostNotiViewHolder> {

	List<User> userList;
	Context context;

	public UserListPostNotiAdapter(List<User> userList, Context context) {
		this.userList = userList;
		this.context = context;
	}

	@NonNull
	@Override
	public UserListPostNotiViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.row_user_post_noti, parent, false);

		UserListPostNotiViewHolder userListPostNotiViewHolder = new UserListPostNotiViewHolder(view);
		return userListPostNotiViewHolder;
	}

	@Override
	public void onBindViewHolder(@NonNull UserListPostNotiViewHolder holder, int position) {
		final User user = userList.get(position);
		if (!user.getImage().equals("")) {
			Picasso.get().load(user.getImage()).networkPolicy(NetworkPolicy.NO_CACHE).memoryPolicy(MemoryPolicy.NO_CACHE).into(holder.img_avatar);
		}
		holder.txt_name.setText(user.getName());
		holder.btn_delete.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				AlertDialog.Builder builder = new AlertDialog.Builder(context);
				builder.setTitle("Unsubscribe");
				builder.setMessage("Do you want to unsubcribe " + user.getName());
				builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						unSubscribe(user.getUid());
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
		});
	}

	private void unSubscribe(final String uid) {
		String myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
		DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Follows").child(myUid).child(uid);
		ref.removeValue();
		notifyDataSetChanged();
	}

	@Override
	public int getItemCount() {
		return userList.size();
	}

	public class UserListPostNotiViewHolder extends RecyclerView.ViewHolder {
		CircularImageView img_avatar;
		TextView txt_name;
		ImageButton btn_delete;
		public UserListPostNotiViewHolder(@NonNull View itemView) {
			super(itemView);

			img_avatar = itemView.findViewById(R.id.img_avatar);
			txt_name = itemView.findViewById(R.id.txt_name);
			btn_delete = itemView.findViewById(R.id.btn_delete);
		}
	}
}
