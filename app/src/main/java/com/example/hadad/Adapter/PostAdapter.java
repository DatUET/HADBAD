package com.example.hadad.Adapter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;


import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.hadad.AddPostActivity;
import com.example.hadad.ListLikedActivity;
import com.example.hadad.Model.Post;
import com.example.hadad.Model.User;
import com.example.hadad.Notification.Data;
import com.example.hadad.Notification.Sender;
import com.example.hadad.Notification.Token;
import com.example.hadad.PostDetailActivity;
import com.example.hadad.R;
import com.example.hadad.ThereProfileActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

	Context context;
	List<Post> postList;
	String nameTran;

	String myUid;
	private DatabaseReference refLikes, refPost;
	boolean isProcessLike = false;
	User user = new User();
	RequestQueue requestQueue;

	public PostAdapter(Context context, List<Post> postList, String nameTran) {
		this.context = context;
		this.postList = postList;
		this.nameTran = nameTran;
		myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
		refLikes = FirebaseDatabase.getInstance().getReference("Likes");
		refPost = FirebaseDatabase.getInstance().getReference("Post");
	}

	// build file layout
	@NonNull
	@Override
	public PostViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.row_post, viewGroup, false);

		PostViewHolder postViewHolder = new PostViewHolder(view);
		return postViewHolder;
	}

	@Override
	public void onBindViewHolder(@NonNull final PostViewHolder postViewHolder, final int i) {
		Post post = postList.get(i);
		final String pId, pTitle, pDescr, pImage, pTimestamp, uid, uEmail, uDp, uName, pLikes, pComments, hostUid, pMode;
		pId = post.getpId();
		pTitle = post.getpTitle();
		pDescr = post.getpDescr();
		pImage = post.getpImage();
		pTimestamp = post.getpTime();
		uid = post.getUid();
		uEmail = post.getuEmail();
		uDp = post.getuDp();
		uName = post.getuName();
		pLikes = post.getpLikes();
		pComments = post.getpComments();
		hostUid = post.getHostUid();
		pMode = post.getpMode();
		final List<String> imgList = new ArrayList<>();
		imgList.addAll(Arrays.asList(pImage.split(",")));
		requestQueue = Volley.newRequestQueue(context.getApplicationContext());

		Calendar cal = Calendar.getInstance(Locale.ENGLISH);
		cal.setTimeInMillis(Long.parseLong(pTimestamp));
		final String pTime = DateFormat.format("dd/MM/yyyy hh:mm aa", cal).toString();

		postViewHolder.txt_name.setText(uName);
		postViewHolder.txt_time.setText(pTime);
		postViewHolder.txt_title.setText(pTitle);
		postViewHolder.txt_description.setText(pDescr);
		postViewHolder.txt_like.setText(pLikes + " Likes");
		postViewHolder.txt_comment.setText(pComments + " Comments");

		if(uid.equals(myUid))
		{
			postViewHolder.btn_share.setVisibility(View.GONE);
		}
		else {
			postViewHolder.btn_share.setVisibility(View.VISIBLE);
		}

		if (pMode.equals("Publish"))
		{
			postViewHolder.img_mode.setImageResource(R.drawable.ic_publish_post);
		}
		else
		{
			postViewHolder.img_mode.setImageResource(R.drawable.ic_private_post);
		}

		try {
			Picasso.get().load(uDp).placeholder(R.drawable.user).into(postViewHolder.img_avatar);
		}
		catch (Exception ex)
		{

		}

		if (!pImage.equals("noImage"))
		{
			postViewHolder.vp_img.setVisibility(View.VISIBLE);
			ImgPostAdapter imgPostAdapter = new ImgPostAdapter(context, imgList, pId);
			postViewHolder.vp_img.setAdapter(imgPostAdapter);
		}
		else
		{
			postViewHolder.vp_img.setVisibility(View.GONE);
		}

		postViewHolder.btn_more.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
					showMoreOption(postViewHolder.btn_more, uid, myUid, pId, imgList, hostUid);
			}
		});

		// check nếu đã like thì hiện nút like xanh
		setLike(postViewHolder, pId);

		postViewHolder.btn_like.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				final int numberLike = Integer.parseInt(pLikes);
				isProcessLike = true;
				refLikes.addValueEventListener(new ValueEventListener() {
					@Override
					public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
						if(isProcessLike)
						{
							if(dataSnapshot.child(pId).hasChild(myUid))
							{
								// nếu đã like bấm lần nữa bỏ like
								refPost.child(pId).child("pLikes").setValue((numberLike - 1) + "");
								refLikes.child(pId).child(myUid).removeValue();
								isProcessLike = false;
							}
							else
							{
								// nếu chưa thì add vào danh sách đã like đồng thời gửi thông báo đến chủ post
								refPost.child(pId).child("pLikes").setValue((numberLike + 1) + "");
								refLikes.child(pId).child(myUid).setValue("Liked");
								isProcessLike = false;
								if (!myUid.equals(uid))
								{
									DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(myUid).child("name");
									ref.addListenerForSingleValueEvent(new ValueEventListener() {
										@Override
										public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
											sendNotification(pId, dataSnapshot.getValue() + "", uid);
										}

										@Override
										public void onCancelled(@NonNull DatabaseError databaseError) {

										}
									});

								}
							}
						}

					}

					@Override
					public void onCancelled(@NonNull DatabaseError databaseError) {

					}
				});
			}
		});

		postViewHolder.btn_comment.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(context, PostDetailActivity.class);
				intent.putExtra("postId", pId);
				context.startActivity(intent);
			}
		});

		postViewHolder.btn_share.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				beginShare(pTitle, pDescr, pImage, hostUid);

			}
		});

		postViewHolder.layout_profile.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(context, ThereProfileActivity.class);
				intent.putExtra("uid", uid);
				ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) context, postViewHolder.img_avatar, ViewCompat.getTransitionName(postViewHolder.img_avatar));
				context.startActivity(intent, activityOptionsCompat.toBundle());
			}
		});

		postViewHolder.layout_post.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(context, PostDetailActivity.class);
				intent.putExtra("postId", pId);
				context.startActivity(intent);
			}
		});

		postViewHolder.txt_like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ListLikedActivity.class);
                intent.putExtra("pId", pId);
                context.startActivity(intent);
            }
        });


	}

	// gửi thông báo đi
	private void sendNotification(final String pId, final String name, final String uid) {
		{
			DatabaseReference allTokens = FirebaseDatabase.getInstance().getReference("Tokens");
			Query query = allTokens.orderByKey().equalTo(uid);
			query.addValueEventListener(new ValueEventListener() {
				@Override
				public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
					for(DataSnapshot snapshot : dataSnapshot.getChildren())
					{
						Token token = snapshot.getValue(Token.class);
						Data data = new Data(pId, name + " liked on your post","Like", uid, "comment", R.drawable.ic_defaut_img);
						Sender sender = new Sender(data, token.getToken());
						try {

							JSONObject senderObj = new JSONObject(new Gson().toJson(sender));
							JsonObjectRequest jsonObjectRequest = new JsonObjectRequest("https://fcm.googleapis.com/fcm/send", senderObj,
									new com.android.volley.Response.Listener<JSONObject>() {
										@Override
										public void onResponse(JSONObject response) {

										}
									}, new Response.ErrorListener() {
								@Override
								public void onErrorResponse(VolleyError error) {

								}
							})
							{
								@Override
								public Map<String, String> getHeaders() throws AuthFailureError {
									Map<String, String> headers = new HashMap<>();
									headers.put("Content-Type", "application/json");
									headers.put("Authorization", "key=AAAAYhgK_pk:APA91bG6syUF2aAKH7gMaROZ8NpZKoH2Fh9oyFvA1ArSwbJJneP0kzCilQbh-WYBYXAAnChRZhhb-qEqR3Plk5V14v1SDX2Tu6_G66he1asQi5pzlfqZaFnNYgP0YkPE1U-lRwWJwQWx");
									return headers;
								}
							};

							requestQueue.add(jsonObjectRequest);
						}
						catch (Exception ex)
						{
							ex.printStackTrace();
						}
					}
				}

				@Override
				public void onCancelled(@NonNull DatabaseError databaseError) {

				}
			});
		}
	}

	private void beginShare(final String pTitle, final String pDescr, final String pImage, final String hostUid) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle("Share");
		builder.setMessage("Do you want to share this  post?");
		builder.setPositiveButton("Share", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(myUid);
				reference.addListenerForSingleValueEvent(new ValueEventListener() {
					@Override
					public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
						user = dataSnapshot.getValue(User.class);
						Log.d("user info", user.getName());

						String timeID = String.valueOf(System.currentTimeMillis());
						HashMap<String, Object> hashMap = new HashMap<>();
						hashMap.put("uid", myUid);
						hashMap.put("uName", user.getName());
						hashMap.put("uEmail", user.getEmail());
						hashMap.put("uDp", user.getImage());
						hashMap.put("pLikes", "0");
						hashMap.put("pComments", "0");
						hashMap.put("pId", timeID);
						hashMap.put("pTitle", pTitle);
						hashMap.put("pDescr", pDescr);
						hashMap.put("pImage", pImage);
						hashMap.put("pTime", timeID);
						hashMap.put("hostUid", hostUid);
						hashMap.put("pMode", "Publish");

						DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Post");
						ref.child(timeID).setValue(hashMap)
								.addOnSuccessListener(new OnSuccessListener<Void>() {
									@Override
									public void onSuccess(Void aVoid) {
										Toast.makeText(context, "Post publised", Toast.LENGTH_LONG).show();
									}
								})
								.addOnFailureListener(new OnFailureListener() {
									@Override
									public void onFailure(@NonNull Exception e) {
										Toast.makeText( context, e.getMessage(), Toast.LENGTH_LONG).show();
									}
								});
					}

					@Override
					public void onCancelled(@NonNull DatabaseError databaseError) {

					}
				});
			}
		});
		builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});

		AlertDialog alertDialog = builder.create();
		alertDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
		alertDialog.show();

	}

	private void setLike(final PostViewHolder holder, final String postKey) {
		refLikes.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				if(dataSnapshot.child(postKey).hasChild(myUid))
				{
					holder.btn_like.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_liked, 0, 0, 0);
					holder.btn_like.setText("Liked");
				}
				else
				{
					holder.btn_like.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_like_black, 0, 0, 0);
					holder.btn_like.setText("Like");
				}
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {

			}
		});
	}

	private void showMoreOption(final ImageButton btn_more, String uid, String myUid, final String pId, final List<String> imgList, final String hostUid) {
		PopupMenu popupMenu = new PopupMenu(context, btn_more, Gravity.END);
		if(myUid.equals(uid)) {
			popupMenu.getMenu().add(Menu.NONE, 0, 0, "Delete");
			popupMenu.getMenu().add(Menu.NONE, 1, 0, "Edit");
		}
		popupMenu.getMenu().add(Menu.NONE, 2, 0, "Detail");
		popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				int id = item.getItemId();
				switch (id) {
					case 0:
						AlertDialog.Builder builder = new AlertDialog.Builder(context);
						builder.setMessage("Are you sure delete this post");
						builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								beginDelete(pId, imgList, hostUid);
							}
						});
						builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
							}
						});
						AlertDialog alertDialog = builder.create();
						alertDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
						alertDialog.show();
						break;

					case 1:
						Intent intent = new Intent(context, AddPostActivity.class);
						intent.putExtra("key", "editPost");
						intent.putExtra("editPostId", pId);
						context.startActivity(intent);
						break;

					case 2:
						Intent intent1 = new Intent(context, PostDetailActivity.class);
						intent1.putExtra("postId", pId);
						context.startActivity(intent1);
						break;
				}
				return false;
			}
		});
		popupMenu.show();
	}

	private void beginDelete(String pId, List<String> imgList, String hostUid) {
		deleteWithoutImage(pId);
		DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Likes").child(pId);
		databaseReference.removeValue();
	}

	// xóa post có ảnh

	// xóa post ko có ảnh
	private void deleteWithoutImage(String pId) {
		final ProgressDialog progressDialog = new ProgressDialog(context);
		progressDialog.setMessage("Deleting...");
		progressDialog.show();

		Query query = FirebaseDatabase.getInstance().getReference("Post").orderByChild("pId").equalTo(pId);
		query.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				for(DataSnapshot snapshot : dataSnapshot.getChildren())
				{
					snapshot.getRef().removeValue();
					progressDialog.dismiss();
					Toast.makeText(context, "Delete successfully", Toast.LENGTH_LONG).show();
				}
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {

			}
		});
	}

	@Override
	public int getItemCount() {
		return postList.size();
	}

	public class PostViewHolder extends RecyclerView.ViewHolder {
		ImageView img_avatar, img_mode;
		TextView txt_name, txt_time, txt_title, txt_description, txt_like, txt_comment;
		Button btn_like, btn_comment, btn_share;
		ImageButton btn_more;
		LinearLayout layout_profile, layout_post;
		ViewPager vp_img;
		public PostViewHolder(@NonNull View itemView) {
			super(itemView);

			img_avatar = itemView.findViewById(R.id.img_avatar);
			img_avatar.setTransitionName(nameTran);
			txt_name = itemView.findViewById(R.id.txt_name);
			txt_time = itemView.findViewById(R.id.txt_time);
			txt_title = itemView.findViewById(R.id.txt_title);
			txt_description = itemView.findViewById(R.id.txt_description);
			txt_like = itemView.findViewById(R.id.txt_like);
			txt_comment = itemView.findViewById(R.id.txt_comment);
			btn_more = itemView.findViewById(R.id.btn_more);
			btn_like = itemView.findViewById(R.id.btn_like);
			btn_comment = itemView.findViewById(R.id.btn_comment);
			btn_share = itemView.findViewById(R.id.btn_share);
			layout_profile = itemView.findViewById(R.id.layout_profile);
			layout_post = itemView.findViewById(R.id.layout_post);
			vp_img = itemView.findViewById(R.id.vp_img);
			img_mode = itemView.findViewById(R.id.img_mode);

		}
	}
}
