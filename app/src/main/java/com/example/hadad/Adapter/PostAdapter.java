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

import android.graphics.Color;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;


public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

	Context context;
	List<Post> postList;
	String nameTran;

	String myUid;
	private DatabaseReference refLikes, refPost, refComment;
	boolean isProcessLike = false;
	User user = new User();
	RequestQueue requestQueue;

	private static final String MAX_TIME = "9999999999999";

	public PostAdapter(Context context, List<Post> postList, String nameTran) {
		this.context = context;
		this.postList = postList;
		this.nameTran = nameTran;
		myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
		refLikes = FirebaseDatabase.getInstance().getReference("Likes");
		refPost = FirebaseDatabase.getInstance().getReference("Post");
		refComment = FirebaseDatabase.getInstance().getReference("Comments");
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
		final String pId, pDescr, pImage, pTimestamp, uid, uEmail, uDp, uName, hostUid, pMode;
		pId = post.getpId();
		pDescr = post.getpDescr();
		pImage = post.getpImage();
		pTimestamp = post.getpTime();
		uid = post.getUid();
		uEmail = post.getuEmail();
		uDp = post.getuDp();
		uName = post.getuName();
		hostUid = post.getHostUid();
		pMode = post.getpMode();
		final List<String> imgList = new ArrayList<>();
		imgList.addAll(Arrays.asList(pImage.split(",")));
		requestQueue = Volley.newRequestQueue(context.getApplicationContext());

		Calendar cal = Calendar.getInstance(Locale.ENGLISH);
		cal.setTimeInMillis(Long.parseLong(pTimestamp));
		final String pTime = DateFormat.format("dd/MM/yyyy hh:mm aa", cal).toString();

		postViewHolder.txt_name.setText(uName);
		postViewHolder.txt_name.setShadowLayer(6, 0, 5, Color.parseColor("#000000"));
		postViewHolder.txt_time.setText(pTime);
		postViewHolder.txt_description.setText(pDescr);
		refLikes.child(pId).addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				postViewHolder.txt_like.setText(dataSnapshot.getChildrenCount() + " Likes");
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {

			}
		});
		refComment.child(pId).addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				postViewHolder.txt_comment.setText(dataSnapshot.getChildrenCount() + " Comments");
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {

			}
		});


		if (uid.equals(myUid)) {
			postViewHolder.btn_share.setVisibility(View.GONE);
		} else {
			postViewHolder.btn_share.setVisibility(View.VISIBLE);
		}

		if (pMode.equals("Public")) {
			postViewHolder.img_mode.setImageResource(R.drawable.ic_publish_post);
		} else {
			postViewHolder.img_mode.setImageResource(R.drawable.ic_private_post);
		}

		try {
			Picasso.get().load(uDp).placeholder(R.drawable.user).into(postViewHolder.img_avatar);
		} catch (Exception ex) {
			Picasso.get().load(R.drawable.user).into(postViewHolder.img_avatar);
		}

		if (!pImage.equals("noImage")) {
			postViewHolder.vp_img.setVisibility(View.VISIBLE);
			ImgPostAdapter imgPostAdapter = new ImgPostAdapter(context, imgList, pId);
			postViewHolder.vp_img.setAdapter(imgPostAdapter);
		} else {
			postViewHolder.vp_img.setVisibility(View.GONE);
		}

		postViewHolder.btn_more.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showMoreOption(postViewHolder.btn_more, uid, myUid, pId, imgList, hostUid, i);
			}
		});

		// check nếu đã like thì hiện nút like xanh
		setLike(postViewHolder, pId);

		postViewHolder.btn_like.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				isProcessLike = true;
				refLikes.addValueEventListener(new ValueEventListener() {
					@Override
					public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
						if (isProcessLike) {
							if (dataSnapshot.child(pId).hasChild(myUid)) {
								// nếu đã like bấm lần nữa bỏ like
								refLikes.child(pId).child(myUid).removeValue();
								isProcessLike = false;
							} else {
								// nếu chưa thì add vào danh sách đã like đồng thời gửi thông báo đến chủ post
								refLikes.child(pId).child(myUid).setValue("Liked");
								isProcessLike = false;
								if (!myUid.equals(uid)) {
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

				beginShare(pDescr, pImage, hostUid);

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
					for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
						Token token = snapshot.getValue(Token.class);
						Data data = new Data(pId, name + " liked on your post", "Like", uid, "comment", R.drawable.user);
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
							}) {
								@Override
								public Map<String, String> getHeaders() throws AuthFailureError {
									Map<String, String> headers = new HashMap<>();
									headers.put("Content-Type", "application/json");
									headers.put("Authorization", "key=AAAAO8U71X8:APA91bFTogEvmtD6vTfETtuEOyh9CloLCGczfPEp6RUT01euNT7RaYnSymNDIqCRkUoPVYZC2K9EXj36Sg7T9pRXwuacsm-IiLS1_xgwSuUO9F1yNBbd0cJacT4qBeZdMVrDZl9MKcc9");
									return headers;
								}
							};

							requestQueue.add(jsonObjectRequest);
						} catch (Exception ex) {
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

	private void beginShare(final String pDescr, final String pImage, final String hostUid) {
		new SweetAlertDialog(context, SweetAlertDialog.NORMAL_TYPE)
				.setTitleText("Share")
				.setContentText("Do you want to share this  post?")
				.setConfirmButton("Yes", new SweetAlertDialog.OnSweetClickListener() {
					@Override
					public void onClick(SweetAlertDialog sweetAlertDialog) {
						sweetAlertDialog.dismiss();
						final SweetAlertDialog sweetAlertDialog1 = new SweetAlertDialog(context, SweetAlertDialog.PROGRESS_TYPE);
						sweetAlertDialog1.setTitleText("Sharing")
								.setContentText("Please wait...")
								.show();
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
								hashMap.put("pId", Long.parseLong(MAX_TIME) - Long.parseLong(timeID) + "");
								hashMap.put("pDescr", pDescr);
								hashMap.put("pImage", pImage);
								hashMap.put("pTime", timeID);
								hashMap.put("hostUid", hostUid);
								hashMap.put("pMode", "Public");

								DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Post");
								ref.child(Long.parseLong(MAX_TIME) - Long.parseLong(timeID) + "").setValue(hashMap)
										.addOnSuccessListener(new OnSuccessListener<Void>() {
											@Override
											public void onSuccess(Void aVoid) {
												sweetAlertDialog1.dismiss();
												new SweetAlertDialog(context, SweetAlertDialog.SUCCESS_TYPE)
														.setTitleText("complete")
														.setContentText("This post was shared!")
														.show();
											}
										})
										.addOnFailureListener(new OnFailureListener() {
											@Override
											public void onFailure(@NonNull Exception e) {
												sweetAlertDialog1.dismiss();
												new SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE)
														.setTitleText("Error")
														.setContentText(e.getMessage())
														.show();
											}
										});
							}

							@Override
							public void onCancelled(@NonNull DatabaseError databaseError) {

							}
						});
					}
				})
				.setCancelButton("No", new SweetAlertDialog.OnSweetClickListener() {
					@Override
					public void onClick(SweetAlertDialog sweetAlertDialog) {
						sweetAlertDialog.dismiss();
					}
				}).show();

	}

	private void setLike(final PostViewHolder holder, final String postKey) {
		refLikes.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				if (dataSnapshot.child(postKey).hasChild(myUid)) {
					holder.img_btnlike.setImageResource(R.drawable.ic_liked);
					holder.txt_btnlike.setText("Liked");
					holder.txt_btnlike.setTextColor(Color.parseColor("#40C4FF"));
				} else {
					holder.img_btnlike.setImageResource(R.drawable.ic_like_black);
					holder.txt_btnlike.setText("Like");
					holder.txt_btnlike.setTextColor(Color.WHITE);
				}
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {

			}
		});
	}

	private void showMoreOption(final ImageButton btn_more, String uid, String myUid, final String pId, final List<String> imgList, final String hostUid, final int i) {
		PopupMenu popupMenu = new PopupMenu(context, btn_more, Gravity.END);
		if (myUid.equals(uid)) {
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
						new SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
								.setTitleText("Delete")
								.setContentText("Are you sure delete this post?")
								.setCancelButton("Yes", new SweetAlertDialog.OnSweetClickListener() {
									@Override
									public void onClick(SweetAlertDialog sweetAlertDialog) {
										beginDelete(pId, imgList, hostUid, i);
										sweetAlertDialog.dismiss();
									}
								})
								.setConfirmButton("No", new SweetAlertDialog.OnSweetClickListener() {
									@Override
									public void onClick(SweetAlertDialog sweetAlertDialog) {
										sweetAlertDialog.dismiss();
									}
								})
								.show();
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

	private void beginDelete(String pId, List<String> imgList, String hostUid, int i) {
		deleteWithoutImage(pId, i);
		DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Likes").child(pId);
		databaseReference.removeValue();
	}

	// xóa post có ảnh

	// xóa post ko có ảnh
	private void deleteWithoutImage(String pId, final int i) {
		final SweetAlertDialog progressDialog = new SweetAlertDialog(context);
		progressDialog.setTitleText("Deleting...");
		progressDialog.show();

		DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Post").child(pId);
		ref.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
			@Override
			public void onComplete(@NonNull Task<Void> task) {
				if (task.isSuccessful()) {
					progressDialog.dismiss();
					new SweetAlertDialog(context, SweetAlertDialog.SUCCESS_TYPE)
							.setTitleText("Delete Successful")
							.show();
					postList.remove(i);
					notifyDataSetChanged();
				}
			}
		})
				.addOnFailureListener(new OnFailureListener() {
					@Override
					public void onFailure(@NonNull Exception e) {
						progressDialog.dismiss();
						new SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE)
								.setTitleText(e.getMessage())
								.show();
					}
				});
	}

	@Override
	public int getItemCount() {
		return postList.size();
	}

	public class PostViewHolder extends RecyclerView.ViewHolder {
		ImageView img_avatar, img_mode, img_btnlike;
		TextView txt_name, txt_time, txt_description, txt_like, txt_comment, txt_btnlike;
		LinearLayout btn_like, btn_comment, btn_share;
		ImageButton btn_more;
		LinearLayout layout_profile, layout_post;
		ViewPager vp_img;

		public PostViewHolder(@NonNull View itemView) {
			super(itemView);

			img_avatar = itemView.findViewById(R.id.img_avatar);
			img_avatar.setTransitionName(nameTran);
			txt_name = itemView.findViewById(R.id.txt_name);
			txt_time = itemView.findViewById(R.id.txt_time);
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
			txt_btnlike = itemView.findViewById(R.id.txt_btnlike);
			img_btnlike = itemView.findViewById(R.id.img_btnlike);
		}
	}
}
