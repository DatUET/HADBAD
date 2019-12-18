package com.example.hadad;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.blogspot.atifsoftwares.circularimageview.CircularImageView;
import com.example.hadad.Adapter.ChatAdapter;
import com.example.hadad.Model.Chat;
import com.example.hadad.Model.User;
import com.example.hadad.Notification.Data;
import com.example.hadad.Notification.Sender;
import com.example.hadad.Notification.Token;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;


public class ChatActivity extends AppCompatActivity {

	private static final int REQUEST_CALL = 200;
	private static final int REQUEST_CAMERA = 100;
	private static final int REQUEST_AUDIO = 300;
	private static final int REQUEST_STORAGE = 400;
	private static final int REQUEST_GALLERY = 500;
	private static final int REQUEST_VIDEO = 600;
	private static final int REQUEST_STORAGE_VIDEO = 700;

	private static final int ITEM_LOAD = 20;
	private static final String MAX_TIME = "9999999999999";

	String last_node = "";
	boolean isLoading = false;

	Toolbar toolbar;
	RecyclerView recycler_chats;
	CircularImageView img_avatar;
	TextView txt_name, txt_online;
	EditText txt_inputmes;
	ImageButton btn_send, btn_send_img, btn_send_video;
	LinearLayout layout_profile, layout_imgchat, layout_videochat;
	ImageView img_chat, img_delete, img_delete_video;
	VideoView video_chat;
	MediaController mediaController;
	SwipeRefreshLayout srl_loadmore;;

	ValueEventListener seenListener;
	DatabaseReference userFefForSeen;
	SweetAlertDialog sweetAlertDialog;

	List<Chat> chatList;
	ChatAdapter chatAdapter;

	FirebaseAuth firebaseAuth;
	FirebaseDatabase firebaseDatabase;
	DatabaseReference usersDbRef;
	String uid, myuid, hisImage;

	Uri imgUri = null, videoUri = null;

	private RequestQueue requestQueue;
	private boolean notify = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);

		addControl();
		addEvent();
	}

	private void addControl() {
		toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		toolbar.setTitle("");
		recycler_chats = findViewById(R.id.recycler_chats);
		img_avatar = findViewById(R.id.img_avatar);
		txt_name = findViewById(R.id.txt_name);
		txt_online = findViewById(R.id.txt_online);
		txt_inputmes = findViewById(R.id.txt_inputmes);
		btn_send = findViewById(R.id.btn_send);
		btn_send_img = findViewById(R.id.btn_send_img);
		btn_send_video = findViewById(R.id.btn_send_video);
		img_chat = findViewById(R.id.img_chat);
		img_delete = findViewById(R.id.img_delete);
		img_delete_video = findViewById(R.id.img_delete_video);
		layout_profile = findViewById(R.id.layout_profile);
		layout_imgchat = findViewById(R.id.layout_imgchat);
		layout_videochat = findViewById(R.id.layout_videochat);
		video_chat = findViewById(R.id.video_chat);
		srl_loadmore = findViewById(R.id.srl_loadmore);
		requestQueue = Volley.newRequestQueue(getApplicationContext());
		sweetAlertDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
		sweetAlertDialog.setCanceledOnTouchOutside(false);

		chatList = new ArrayList<>();
		LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
		linearLayoutManager.setStackFromEnd(true);
		recycler_chats.setHasFixedSize(true);
		recycler_chats.setLayoutManager(linearLayoutManager);


		firebaseAuth = FirebaseAuth.getInstance();
		Intent intent = getIntent();
		uid = intent.getStringExtra("uid");

		firebaseDatabase = FirebaseDatabase.getInstance();
		usersDbRef = firebaseDatabase.getReference("Users");

		Query userQuery = usersDbRef.orderByChild("uid").equalTo(uid);
		userQuery.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				for (DataSnapshot snapshot : dataSnapshot.getChildren())
				{
					String name = snapshot.child("name").getValue() + "";
					hisImage = snapshot.child("image").getValue() + "";
					String onlineStatus = snapshot.child("onlineStatus").getValue() + "";
					String typingStatus = snapshot.child("typingTo").getValue() + "";

					txt_name.setText(name);
					if (typingStatus.equals(myuid))
					{
						txt_online.setText("Typing...");
					}
					else {
						if (onlineStatus.equals("online")) {
							txt_online.setText(onlineStatus);
						} else {
							Calendar cal = Calendar.getInstance(Locale.ENGLISH);
							cal.setTimeInMillis(Long.parseLong(onlineStatus));
							String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa", cal).toString();
							txt_online.setText("Log out at: " + dateTime);
						}
					}

					try {
						if(!TextUtils.isEmpty(hisImage)) {
							Picasso.get().load(hisImage).placeholder(R.drawable.user).networkPolicy(NetworkPolicy.NO_CACHE).memoryPolicy(MemoryPolicy.NO_CACHE).into(img_avatar);
						}
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

	private void addEvent() {

		btn_send.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				notify = true;
				String message = txt_inputmes.getText().toString();
				if(TextUtils.isEmpty(message) && imgUri == null && videoUri == null)
				{

				}
				else
				{
					sendMessage(message);
				}
				txt_inputmes.setText("");
				layout_imgchat.setVisibility(View.GONE);
				layout_videochat.setVisibility(View.GONE);
				imgUri = null;
				videoUri = null;
			}
		});

		txt_inputmes.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if(s.toString().trim().length() == 0)
				{
					checkTypingStatus("noOne");
				}
				else
				{
					checkTypingStatus(uid);
				}
			}

			@Override
			public void afterTextChanged(Editable s) {

			}
		});

		layout_profile.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(ChatActivity.this, ThereProfileActivity.class);
				intent.putExtra("uid", uid);
				startActivity(intent);
			}
		});

		btn_send_img.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				pickImage();
			}
		});

		img_delete.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				layout_imgchat.setVisibility(View.GONE);
				img_chat.setImageDrawable(null);
				imgUri = null;
			}
		});

		img_delete_video.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				layout_videochat.setVisibility(View.GONE);
				video_chat.setVideoURI(null);
				videoUri = null;
			}
		});

		btn_send_video.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				pickVideo();
			}
		});

		video_chat.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(video_chat.isPlaying())
				{
					video_chat.pause();
				}
				else
				{
					video_chat.start();
				}
				return false;
			}
		});

		readMessage();
		seenMessage();

		srl_loadmore.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				isLoading = false;
				loadMoreData();
			}
		});
	}

	private void pickVideo() {
		if(ContextCompat.checkSelfPermission(ChatActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
		{
			ActivityCompat.requestPermissions(ChatActivity.this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_STORAGE_VIDEO);
		}
		else
		{
			Intent intent = new Intent(Intent.ACTION_PICK);
			intent.setType("video/*");
			startActivityForResult(intent, REQUEST_VIDEO);
		}
	}

	private void pickImage() {
		if(ContextCompat.checkSelfPermission(ChatActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
		{
			ActivityCompat.requestPermissions(ChatActivity.this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_STORAGE);
		}
		else
		{
			Intent intent = new Intent(Intent.ACTION_PICK);
			intent.setType("image/*");
			startActivityForResult(intent, REQUEST_GALLERY);
		}
	}

	private void seenMessage() {
		userFefForSeen = FirebaseDatabase.getInstance().getReference("Chats");
		seenListener = userFefForSeen.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				for(DataSnapshot snapshot : dataSnapshot.getChildren())
				{
					Chat chat = snapshot.getValue(Chat.class);
					if(chat.getReciver().equals(myuid) && chat.getSender().equals(uid))
					{
						HashMap<String, Object> hashMap = new HashMap<>();
						hashMap.put("isseen", true);
						snapshot.getRef().updateChildren(hashMap);
					}
				}
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {

			}
		});
	}

	private void readMessage()
	{
		Query query1;
		if(TextUtils.isEmpty(last_node))
		{
			query1 = FirebaseDatabase.getInstance().getReference().child("Chats").orderByKey();
		}
		else
		{
			query1 = FirebaseDatabase.getInstance().getReference().child("Chats").orderByKey().endAt(last_node);

		}
		query1.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				chatList.clear();
				int countItemChat = 0;
				for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
					Chat chat = snapshot.getValue(Chat.class);
					if (((chat.getReciver().equals(myuid) && chat.getSender().equals(uid)) ||
							chat.getReciver().equals(uid) && chat.getSender().equals(myuid)) && countItemChat < ITEM_LOAD ) {
						chatList.add(chat);
						last_node = snapshot.getKey();
						countItemChat++;
					}
				}
				Collections.reverse(chatList);
				chatAdapter = new ChatAdapter(ChatActivity.this, chatList, hisImage);
				chatAdapter.notifyDataSetChanged();
				recycler_chats.setAdapter(chatAdapter);
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {

			}
		});

		Log.d("Last key", last_node);
	}

	private void loadMoreData()
	{
		if(!isLoading)
		{
			Query query;
			if(TextUtils.isEmpty(last_node))
			{
				query = FirebaseDatabase.getInstance().getReference().child("Chats").orderByKey();
			}
			else
			{
				query = FirebaseDatabase.getInstance().getReference().child("Chats").orderByKey().startAt(last_node);

			}
			query.addListenerForSingleValueEvent(new ValueEventListener() {
				@Override
				public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
					Collections.reverse(chatList);
					int countItemChat = 0;
					List<Chat> chatListTemp = new ArrayList<>();
					chatListTemp.clear();
					for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
						Chat chat = snapshot.getValue(Chat.class);
						if (((chat.getReciver().equals(myuid) && chat.getSender().equals(uid)) ||
								chat.getReciver().equals(uid) && chat.getSender().equals(myuid)) && countItemChat < ITEM_LOAD && snapshot.hasChildren()) {
							chatListTemp.add(chat);
							countItemChat++;
							last_node = snapshot.getKey();
							if (countItemChat == ITEM_LOAD) {
								isLoading = true;
							}
							if (!snapshot.hasChildren()) {
								isLoading = true;
								last_node = "end";
							}
						}
					}
					if(!chatListTemp.isEmpty() && !chatList.isEmpty())
					{
						chatList.remove(chatList.size() - 1);
					}
					chatList.addAll(chatListTemp);
					Collections.reverse(chatList);
					chatAdapter = new ChatAdapter(ChatActivity.this, chatList, hisImage);
					chatAdapter.notifyDataSetChanged();
					recycler_chats.setAdapter(chatAdapter);
					recycler_chats.scrollToPosition(chatList.size() - ITEM_LOAD);
				}

				@Override
				public void onCancelled(@NonNull DatabaseError databaseError) {

				}
			});
		}
		//Log.d("Last key", last_node);
		srl_loadmore.setRefreshing(false);
	}

	private void sendMessage(final String message) {
		final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

		final String timeStamp = String.valueOf(System.currentTimeMillis());
		final HashMap<String, Object> hashMap = new HashMap<>();
		hashMap.put("sender", myuid);
		hashMap.put("reciver", uid);
		hashMap.put("timestamp", timeStamp);
		hashMap.put("message", message);
		hashMap.put("isseen", false);
		if(imgUri == null && videoUri == null)
		{
			hashMap.put("image", "noImage");
			hashMap.put("video", "noVideo");
			databaseReference.child("Chats").child(Long.parseLong(MAX_TIME) - Long.parseLong(timeStamp) + "").setValue(hashMap);
		}
		else
		{
			if(videoUri != null) {
				sweetAlertDialog.setTitleText("Sending video");
				sweetAlertDialog.setContentText(" Please wait...");
				sweetAlertDialog.show();
				String filepathAndName = "ChatsVideo/" + "chat_" + timeStamp;
				StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(filepathAndName);
				storageReference.putFile(videoUri)
						.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
							@Override
							public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
								Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
								while (!uriTask.isSuccessful()) ;
								String downloadUri = uriTask.getResult().toString();
								if (uriTask.isSuccessful()) {
									hashMap.put("image", "noImage");
									hashMap.put("video", downloadUri);
									databaseReference.child("Chats").child(Long.parseLong(MAX_TIME) - Long.parseLong(timeStamp) + "").setValue(hashMap);
									sweetAlertDialog.dismiss();
								}
							}
						})
						.addOnFailureListener(new OnFailureListener() {
							@Override
							public void onFailure(@NonNull Exception e) {
								Toast.makeText(ChatActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
							}
						});
			}
			else
			{
				if(imgUri != null) {
					sweetAlertDialog.setTitleText("Sending image");
					sweetAlertDialog.setContentText(" Please wait...");
					sweetAlertDialog.show();
					String filepathAndName = "ChatsImg/" + "chat_" + timeStamp;
					int quality = 100;
					try {
						InputStream is=  getContentResolver().openInputStream(imgUri);
						int byte_size = is.available();
						int file_size=byte_size/1024;
						Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imgUri);
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						if (file_size > 1024)
							quality = (int) (1536.0 / file_size * 100.0);
						bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
						Log.d("quality", quality + "");
						byte[] data = baos.toByteArray();
						StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(filepathAndName);
						storageReference.putBytes(data)
								.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
									@Override
									public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
										Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
										while (!uriTask.isSuccessful()) ;

										String downloadUri = uriTask.getResult().toString();
										if (uriTask.isSuccessful()) {
											hashMap.put("image", downloadUri);
											hashMap.put("video", "noVideo");
											databaseReference.child("Chats").child(Long.parseLong(MAX_TIME) - Long.parseLong(timeStamp) + "").setValue(hashMap);
											sweetAlertDialog.dismiss();
										}
									}
								})
								.addOnFailureListener(new OnFailureListener() {
									@Override
									public void onFailure(@NonNull Exception e) {
										Toast.makeText(ChatActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
									}
								});
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}
			}
		}
		final String msg;
		if (imgUri != null)
		{
			msg = "sent a photo for you";
		}
		else if(videoUri != null)
		{
			msg = "sent a video for you";
		}
		else
		{
			msg = message;
		}
		DatabaseReference database = FirebaseDatabase.getInstance().getReference("Users").child(myuid);
		database.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				User user = dataSnapshot.getValue(User.class);
				if(notify)
				{
					sendNotification(uid, user.getName(), msg);
				}
				notify = false;
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {

			}
		});
	}

	private void sendNotification(final String uid, final String name, final String message) {
		DatabaseReference allTokens = FirebaseDatabase.getInstance().getReference("Tokens");
		Query query = allTokens.orderByKey().equalTo(uid);
		query.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				for(DataSnapshot snapshot : dataSnapshot.getChildren())
				{
					Token token = snapshot.getValue(Token.class);
					Data data = new Data(myuid, name + ":" + message, "New Message", uid,"chat" , R.drawable.user);
					Sender sender = new Sender(data, token.getToken());
					try {

						JSONObject senderObj = new JSONObject(new Gson().toJson(sender));
						JsonObjectRequest jsonObjectRequest = new JsonObjectRequest("https://fcm.googleapis.com/fcm/send", senderObj,
								new Response.Listener<JSONObject>() {
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
								headers.put("Authorization", "key=AAAAO8U71X8:APA91bFTogEvmtD6vTfETtuEOyh9CloLCGczfPEp6RUT01euNT7RaYnSymNDIqCRkUoPVYZC2K9EXj36Sg7T9pRXwuacsm-IiLS1_xgwSuUO9F1yNBbd0cJacT4qBeZdMVrDZl9MKcc9");
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

	private void checkUserStatus()
	{
		FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
		if(firebaseUser != null) {
			myuid = firebaseUser.getUid();
		}
		else
		{
			Intent intent = new Intent(ChatActivity.this, MainActivity.class);
			startActivity(intent);
			finish();
		}
	}

	private void checkOnlineStatus(String status)
	{
		DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Users").child(myuid);
		HashMap<String, Object> hashMap = new HashMap<>();
		hashMap.put("onlineStatus", status);
		dbRef.updateChildren(hashMap);
	}

	private void checkTypingStatus(String typing)
	{
		DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Users").child(myuid);
		HashMap<String, Object> hashMap = new HashMap<>();
		hashMap.put("typingTo", typing);
		dbRef.updateChildren(hashMap);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_main, menu);
		menu.findItem(R.id.it_search).setVisible(false);
		menu.findItem(R.id.it_add_post).setVisible(false);
		menu.findItem(R.id.it_call_phone).setVisible(true);
		menu.findItem(R.id.it_setting).setVisible(false);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if(id == R.id.it_logout) {
			String timestamp = String.valueOf(System.currentTimeMillis());
			checkOnlineStatus(timestamp);
			firebaseAuth.signOut();
			checkUserStatus();
		}

		else if(id == R.id.it_call_phone) {
			callPhone();
		}

//			case R.id.it_video_call:
//				videoCall();
		return super.onOptionsItemSelected(item);
	}

	private void callPhone() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Do you want to call " + txt_name.getText().toString() +"?");
		builder.setPositiveButton("Call", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				makeCall();
				dialog.dismiss();
			}
		});
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		AlertDialog alertDialog = builder.create();
		alertDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
		alertDialog.show();
	}

	private void makeCall() {
		DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(uid).child("phone");
		reference.addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				String phone = dataSnapshot.getValue(String.class);
				if(phone.trim().length() > 0)
				{
					if(ContextCompat.checkSelfPermission(ChatActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED)
					{
						ActivityCompat.requestPermissions(ChatActivity.this, new String[] {Manifest.permission.CALL_PHONE}, REQUEST_CALL);
					}
					else
					{
						String dial = "tel:" + phone;
						startActivity(new Intent(Intent.ACTION_CALL, Uri.parse(dial)));
					}
				}
				else
				{
					Toast.makeText(ChatActivity.this, "This user has not added a phone number yet", Toast.LENGTH_LONG).show();
				}
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {

			}
		});
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		if(requestCode == REQUEST_CALL)
		{
			if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
			{
				makeCall();
			}
			else
			{
				Toast.makeText(ChatActivity.this, "Permission Denied!!!", Toast.LENGTH_LONG).show();
			}
		}
		else if (requestCode == REQUEST_STORAGE)
		{
			if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
			{
				pickImage();
			}
			else
			{
				Toast.makeText(ChatActivity.this, "Permission Denied!!!", Toast.LENGTH_LONG).show();

			}
		}
		else if (requestCode == REQUEST_STORAGE_VIDEO)
		{
			if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
			{
				pickVideo();
			}
			else
			{
				Toast.makeText(ChatActivity.this, "Permission Denied!!!", Toast.LENGTH_LONG).show();

			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		if(resultCode == RESULT_OK)
		{
			if(requestCode == REQUEST_GALLERY)
			{
				imgUri = data.getData();
				layout_imgchat.setVisibility(View.VISIBLE);
				img_chat.setImageURI(imgUri);
			}
			else if(requestCode == REQUEST_VIDEO)
			{
				videoUri = data.getData();
				layout_videochat.setVisibility(View.VISIBLE);
				video_chat.setVideoURI(videoUri);
//				mediaController = new MediaController(ChatActivity.this);
//				mediaController.setAnchorView(video_chat);
//				video_chat.setMediaController(mediaController);
				video_chat.start();
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onStart() {
		super.onStart();
		checkUserStatus();
		checkOnlineStatus("online");
	}

	@Override
	protected void onPause() {
		checkTypingStatus("noOne");
		userFefForSeen.removeEventListener(seenListener);
		super.onPause();
	}

}
