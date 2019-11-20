package com.example.hadad;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import com.example.hadad.Adapter.CommentAdapter;
import com.example.hadad.Adapter.ImgPostDetailAdapter;
import com.example.hadad.Model.Comment;
import com.example.hadad.Model.User;
import com.example.hadad.Notification.Data;
import com.example.hadad.Notification.Sender;
import com.example.hadad.Notification.Token;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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


public class PostDetailActivity extends AppCompatActivity {

	ActionBar actionBar;
	ImageView img_avatar, img_avatar_comment;
	TextView txt_name, txt_time, txt_title, txt_description, txt_like, txt_comment;
	Button btn_like, btn_comment, btn_share;
	ImageButton btn_more, btn_send;
	LinearLayout layout_profile;
	EditText txt_inputcomment;
	RecyclerView recycler_comments;
	ViewPager vp_img;
	List<Comment> commentList;
	CommentAdapter commentAdapter;
	ProgressDialog progressDialog;

	RequestQueue requestQueue;

	String myUid, myEmail, myName, myDp,
	postId, pLikes, hisDp, hisName, hisUid, pImage, hostUid;
	List<String> arrUserCommented, imgList;

	boolean isProcessComment = false, isProcessLike = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_post_detail);
		
		addControl();
		addEvent();
	}

	private void addControl() {
		actionBar = getSupportActionBar();
		actionBar.setTitle("Post Detail");
		actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#2d3447")));
		actionBar.setDisplayShowHomeEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);

		img_avatar = findViewById(R.id.img_avatar);
		txt_name = findViewById(R.id.txt_name);
		txt_time = findViewById(R.id.txt_time);
		txt_title = findViewById(R.id.txt_title);
		txt_description = findViewById(R.id.txt_description);
		txt_like = findViewById(R.id.txt_like);
		txt_comment = findViewById(R.id.txt_comment);
		btn_more = findViewById(R.id.btn_more);
		btn_like = findViewById(R.id.btn_like);
		btn_comment = findViewById(R.id.btn_comment);
		btn_share = findViewById(R.id.btn_share);
		layout_profile = findViewById(R.id.layout_profile);
		img_avatar_comment = findViewById(R.id.img_avatar_comment);
		btn_send = findViewById(R.id.btn_send);
		txt_inputcomment =findViewById(R.id.txt_inputcomment);
		arrUserCommented = new ArrayList<>();
		progressDialog = new ProgressDialog(this);

		requestQueue = Volley.newRequestQueue(getApplicationContext());

		recycler_comments = findViewById(R.id.recycler_comments);
		LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
		linearLayoutManager.setStackFromEnd(true);
		recycler_comments.setHasFixedSize(true);
		recycler_comments.setLayoutManager(linearLayoutManager);

		imgList = new ArrayList<>();
		vp_img = findViewById(R.id.vp_img);

		//Lấy postId từ postAdapter
		Intent intent = getIntent();
		postId = intent.getStringExtra("postId");

		loadPostInfo();
		loadComments();
		checkUserStatus();
		loadUserInfor();
		setLike();
	}

	private void addEvent() {
		btn_send.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				postComment();
			}
		});

		btn_like.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				likePost();
			}
		});

		btn_share.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String pTitle = txt_title.getText().toString();
				String pDescr = txt_description.getText().toString();
				beginShare(pTitle, pDescr, pImage, hostUid);
			}
		});

		btn_more.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showMoreOption();
			}
		});

		txt_like.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(PostDetailActivity.this, ListLikedActivity.class);
				intent.putExtra("pId", postId);
				startActivity(intent);
			}
		});
	}

	private void beginShare(final String pTitle, final String pDescr, final String pImage, final String hostUid) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Share");
		builder.setMessage("Do you want to share this  post?");
		builder.setPositiveButton("Share", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(myUid);
				reference.addListenerForSingleValueEvent(new ValueEventListener() {
					@Override
					public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
						User user = dataSnapshot.getValue(User.class);
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


						DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Post");
						ref.child(timeID).setValue(hashMap)
								.addOnSuccessListener(new OnSuccessListener<Void>() {
									@Override
									public void onSuccess(Void aVoid) {
										Toast.makeText(PostDetailActivity.this, "Post publised", Toast.LENGTH_LONG).show();
									}
								})
								.addOnFailureListener(new OnFailureListener() {
									@Override
									public void onFailure(@NonNull Exception e) {
										Toast.makeText( PostDetailActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
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

	private void showMoreOption() {
		PopupMenu popupMenu = new PopupMenu(this, btn_more, Gravity.END);
		if(hisUid.equals(myUid)) {
			popupMenu.getMenu().add(Menu.NONE, 0, 0, "Delete");
			popupMenu.getMenu().add(Menu.NONE, 1, 0, "Edit");
		}
		popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				int id = item.getItemId();
				switch (id) {
					case 0:
						beginDelete();
						break;

					case 1:
						Intent intent = new Intent(PostDetailActivity.this, AddPostActivity.class);
						intent.putExtra("key", "editPost");
						intent.putExtra("editPostId", postId);
						startActivity(intent);
						break;

				}
				return false;
			}
		});
		popupMenu.show();
	}

	private void beginDelete() {
		deleteWithoutImage();
		DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Likes").child(postId);
		databaseReference.removeValue();
	}


	private void deleteWithoutImage() {
		final ProgressDialog progressDialog = new ProgressDialog(PostDetailActivity.this);
		progressDialog.setMessage("Deleting...");
		progressDialog.show();

		Query query = FirebaseDatabase.getInstance().getReference("Post").orderByChild("pId").equalTo(postId);
		query.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				for(DataSnapshot snapshot : dataSnapshot.getChildren())
				{
					snapshot.getRef().removeValue();
					progressDialog.dismiss();
					Toast.makeText(PostDetailActivity.this, "Delete successfully", Toast.LENGTH_LONG).show();
					finish();
				}
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {

			}
		});
	}

	private void loadComments() {
		commentList = new ArrayList<>();
		DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Comments").child(postId);
		ref.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				commentList.clear();
				for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
					Comment comment = snapshot.getValue(Comment.class);

					commentList.add(comment);
				}
				commentAdapter = new CommentAdapter(PostDetailActivity.this, commentList, myUid, postId);
				recycler_comments.setAdapter(commentAdapter);
				commentAdapter.notifyDataSetChanged();
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
			}
		});

	}

	private void setLike() {
		DatabaseReference refLikes = FirebaseDatabase.getInstance().getReference().child("Likes");
		refLikes.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				if(dataSnapshot.child(postId).hasChild(myUid))
				{
					btn_like.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_liked, 0, 0, 0);
					btn_like.setText("Liked");
				}
				else
				{
					btn_like.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_like_black, 0, 0, 0);
					btn_like.setText("Like");
				}
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {

			}
		});
	}

	private void postComment() {
		String comment = txt_inputcomment.getText().toString().trim();
		if(!TextUtils.isEmpty(comment))
		{
			progressDialog.setMessage("Adding Comment...");
			progressDialog.show();
			DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Comments").child(postId);
			String timestam = String.valueOf(System.currentTimeMillis());

			HashMap<String, Object> hashMap = new HashMap<>();
			hashMap.put("cId", timestam);
			hashMap.put("comment", comment);
			hashMap.put("timestamp", timestam);
			hashMap.put("uId", myUid);
			hashMap.put("uEmail", myEmail);
			hashMap.put("uDp", myDp);
			hashMap.put("uName", myName);

			ref.child(timestam).setValue(hashMap)
					.addOnSuccessListener(new OnSuccessListener<Void>() {
						@Override
						public void onSuccess(Void aVoid) {
							progressDialog.dismiss();
							Toast.makeText(PostDetailActivity.this, "Comment Added", Toast.LENGTH_LONG).show();
							txt_inputcomment.setText("");
							DatabaseReference refsendNoti = FirebaseDatabase.getInstance().getReference("Comments").child(postId);
							refsendNoti.addListenerForSingleValueEvent(new ValueEventListener() {
								@Override
								public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
									for(DataSnapshot snapshot : dataSnapshot.getChildren())
									{
										String uid = snapshot.child("uId").getValue() + "";
										if(!arrUserCommented.contains(uid))
										{
											sendNotification(postId, uid, "New Comment", myName + " commented on your post");
											arrUserCommented.add(uid);
										}
									}
								}

								@Override
								public void onCancelled(@NonNull DatabaseError databaseError) {

								}
							});
							updateCommentCount();
						}
					})
					.addOnFailureListener(new OnFailureListener() {
						@Override
						public void onFailure(@NonNull Exception e) {
							progressDialog.dismiss();
							Toast.makeText(PostDetailActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
						}
					});
		}
	}

	private void sendNotification(final String postId, final String uid, final String title, final String body) {
		DatabaseReference allTokens = FirebaseDatabase.getInstance().getReference("Tokens");
		Query query = allTokens.orderByKey().equalTo(uid);
		query.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				for(DataSnapshot snapshot : dataSnapshot.getChildren())
				{
					Token token = snapshot.getValue(Token.class);
					Data data = new Data(postId, body,title, uid, "comment", R.drawable.ic_defaut_img);
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

	private void likePost() {
		final DatabaseReference refLikes = FirebaseDatabase.getInstance().getReference().child("Likes");
		final DatabaseReference refPost = FirebaseDatabase.getInstance().getReference().child("Post");
		isProcessLike = true;
		refLikes.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				if(isProcessLike)
				{
					if(dataSnapshot.child(postId).hasChild(myUid))
					{
						refPost.child(postId).child("pLikes").setValue((Integer.parseInt(pLikes) - 1) + "");
						refLikes.child(postId).child(myUid).removeValue();
						isProcessLike = false;

					}
					else
					{
						refPost.child(postId).child("pLikes").setValue((Integer.parseInt(pLikes) + 1) + "");
						refLikes.child(postId).child(myUid).setValue("Liked");
						isProcessLike = false;
						if (!myUid.equals(hisUid))
						{
							DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(myUid).child("name");
							ref.addListenerForSingleValueEvent(new ValueEventListener() {
								@Override
								public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
									sendNotification(postId, hisUid, "New Like", dataSnapshot.getValue() + " liked your post");
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

	private void updateCommentCount() {
		isProcessComment = true;
		arrUserCommented.clear();
		arrUserCommented.add(myUid);
		final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Post").child(postId);
		ref.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				if(isProcessComment)
				{
					String commnets = dataSnapshot.child("pComments").getValue() + "";
					int newCommentVal = Integer.parseInt(commnets) + 1;
					ref.child("pComments").setValue(newCommentVal + "");
					isProcessComment = false;
				}
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {

			}
		});
	}

	private void loadUserInfor() {
		Query myRef = FirebaseDatabase.getInstance().getReference("Users");
		myRef.orderByChild("uid").equalTo(myUid).addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				for(DataSnapshot snapshot : dataSnapshot.getChildren())
				{
					myName = snapshot.child("name").getValue() + "";
					myDp = snapshot.child("image").getValue() + "";
				}
				try {
					Picasso.get().load(myDp).placeholder(R.drawable.ic_defaut_img).into(img_avatar_comment);
				}
				catch (Exception ex)
				{
					//Picasso.get().load(R.drawable.ic_defaut_img).into(img_avatar_comment);
				}
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {

			}
		});
	}

	private void loadPostInfo() {
		DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Post");
		Query query = ref.orderByChild("pId").equalTo(postId);
		query.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				for (DataSnapshot snapshot : dataSnapshot.getChildren())
				{
					imgList.clear();
					String pTitle = snapshot.child("pTitle").getValue() + "";
					String pDescr = snapshot.child("pDescr").getValue() + "";
					pLikes = snapshot.child("pLikes").getValue() + "";
					String pTimeStamp = snapshot.child("pTime").getValue() + "";
					pImage = snapshot.child("pImage").getValue() + "";
					hisDp = snapshot.child("uDp").getValue() + "";
					hisUid = snapshot.child("uid").getValue() + "";
					String uEmail = snapshot.child("uEmail").getValue() + "";
					hisName = snapshot.child("uName").getValue() + "";
					String commentCount = snapshot.child("pComments").getValue() + "";
					hostUid = snapshot.child("hostUid").getValue() + "";
					String uidOfPost = snapshot.child("uid").getValue() + "";

					Calendar cal = Calendar.getInstance(Locale.ENGLISH);
					cal.setTimeInMillis(Long.parseLong(pTimeStamp));
					String pTime = DateFormat.format("dd/MM/yyyy hh:mm aa", cal).toString();

					txt_title.setText(pTitle);
					txt_time.setText(pTime);
					txt_description.setText(pDescr);
					txt_like.setText(pLikes + " Likes");
					txt_comment.setText(commentCount + " Comment");
					txt_name.setText(hisName);
					if (myUid.equals(uidOfPost))
					{
						btn_share.setVisibility(View.GONE);
					}

					//lấy ảnh của post
					if(!pImage.equals("noImage"))
					{
						vp_img.setVisibility(View.VISIBLE);
						imgList.addAll(Arrays.asList(pImage.split(",")));
						ImgPostDetailAdapter imgPostDetailAdapter = new ImgPostDetailAdapter(PostDetailActivity.this, imgList);
						vp_img.setAdapter(imgPostDetailAdapter);
					}
					else
					{
						vp_img.setVisibility(View.GONE);
					}

					try {
						Picasso.get().load(hisDp).placeholder(R.drawable.ic_defaut_img).into(img_avatar);
					}
					catch (Exception ex)
					{

					}
				}
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {

			}
		});
	}

	private void checkUserStatus()
	{
		FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
		if(firebaseUser != null)
		{
			myUid = firebaseUser.getUid();
			myEmail = firebaseUser.getEmail();

		}
		else
		{
			Intent intent = new Intent(PostDetailActivity.this, MainActivity.class);
			startActivity(intent);
			finish();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_main, menu);
		menu.findItem(R.id.it_search).setVisible(false);
		menu.findItem(R.id.it_add_post).setVisible(false);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if(id == R.id.it_logout)
		{
			String timestamp = String.valueOf(System.currentTimeMillis());
			checkOnlineStatus(timestamp);
			FirebaseAuth.getInstance().signOut();
			checkUserStatus();
		}
		else if(id == R.id.it_setting)
		{
			Intent intent = new Intent(PostDetailActivity.this, SettingActivity.class);
			startActivity(intent);
		}
		return super.onOptionsItemSelected(item);
	}

	private void checkOnlineStatus(String status)
	{
		DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Users").child(myUid);
		HashMap<String, Object> hashMap = new HashMap<>();
		hashMap.put("onlineStatus", status);
		dbRef.updateChildren(hashMap);
	}

	@Override
	public boolean onSupportNavigateUp() {
		onBackPressed();
		return super.onSupportNavigateUp();
	}

	@Override
	protected void onPause() {

		super.onPause();
	}
}
