package com.example.hadad;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.constraintlayout.solver.widgets.Snapshot;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.hadad.Adapter.ImgAddPostAdapter;
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
import com.google.gson.JsonObject;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddPostActivity extends AppCompatActivity {

	private static final int CAMERA_REQUEST_CODE = 100;
	private static final int STORAGE_REQUEST_CODE = 200;
	private static final int IMAGE_PICK_GALLERY_CODE = 300;
	private static final int IMAGE_PICK_CAMERA_CODE = 400;

	String cameraPermission[];
	String storagePermisstion[];

	ActionBar actionBar;
	EditText txt_inputtitle, txt_description;
	//ImageView img_post;
	Button btn_upload;
	ProgressDialog progressDialog;
	TextView txt_add_img;
	LinearLayout frame_img_post;

	FirebaseAuth firebaseAuth;
	DatabaseReference reference;

	String name, email, uid, dp;
	String editTitle, editDescr, editImage, updateKey = "", editPostId = "", editMode = "", hostUid = "";
	RecyclerView recycler_img_add_post;
	Spinner sp_mode;
	List<String> modeList;
	ArrayAdapter<String> modeAdapter;
	RequestQueue requestQueue;

	List<Uri> uriList;
	ImgAddPostAdapter imgAddPostAdapter;
	Uri imageUri = null;

	String urlImg = "";
	String[] arrCurrentImg;
	int n, positionMode = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_post);

		addControl();
		addEvent();
	}

	private void addEvent() {

		txt_add_img.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				showImagePickDialog();
			}
		});

        recycler_img_add_post.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView recyclerView, @NonNull MotionEvent motionEvent) {
                return false;
            }

            @Override
            public void onTouchEvent(@NonNull RecyclerView recyclerView, @NonNull MotionEvent motionEvent) {

            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean b) {

            }
        });

        sp_mode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				positionMode = position;
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});

		btn_upload.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String title = txt_inputtitle.getText().toString();
				String description = txt_description.getText().toString();

				if(TextUtils.isEmpty(title))
				{
					Toast.makeText(AddPostActivity.this, "Enter title...", Toast.LENGTH_LONG).show();
					return;
				}
				if(TextUtils.isEmpty(description))
				{
					Toast.makeText(AddPostActivity.this, "Enter description...", Toast.LENGTH_LONG).show();
					return;
				}
				if(updateKey.equals("editPost"))
				{
					beginUpdate(title, description, editPostId);
				}
				else
				{
					uploadData(title, description);
				}
			}
		});
	}

	private void beginUpdate(String title, String description, String editPostId) {
		progressDialog.setMessage("Updating Post....");
		progressDialog.show();

		if(!editImage.equals("noImage") && !uriList.isEmpty())
		{
			updateWithImage(title, description, editPostId);
		}
		else if(!editImage.equals("noImage") && uriList.isEmpty())
		{
			updateDaleteImage(title, description, editImage);
		}
		else if(!uriList.isEmpty())
		{
			updateWithNowImage(title, description, editPostId);
		}
		else
		{
			updateWithoutImage(title, description);
		}
	}

	private void updateDaleteImage(final String title, final String description, String editImage) {
		HashMap<String, Object> hashMap = new HashMap<>();
		hashMap.put("uid", uid);
		hashMap.put("uName", name);
		hashMap.put("uEmail", email);
		hashMap.put("uDp", dp);
		hashMap.put("pTitle", title);
		hashMap.put("pDescr", description);
		hashMap.put("pImage", "noImage");
		hashMap.put("pMode", modeList.get(positionMode));

		DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Post");
		ref.child(editPostId).updateChildren(hashMap)
				.addOnSuccessListener(new OnSuccessListener<Void>() {
					@Override
					public void onSuccess(Void aVoid) {
						progressDialog.dismiss();
						Toast.makeText(AddPostActivity.this, "Updated", Toast.LENGTH_LONG).show();
						finish();
					}
				})
				.addOnFailureListener(new OnFailureListener() {
					@Override
					public void onFailure(@NonNull Exception e) {
						progressDialog.dismiss();
						Toast.makeText(AddPostActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
					}
				});
		if(uid.equals(hostUid)) {
			for (String item : arrCurrentImg) {
				StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(item);
				storageReference.delete();
			}
		}
	}

	private void updateWithoutImage(String title, String description) {
		HashMap<String, Object> hashMap = new HashMap<>();
		hashMap.put("uid", uid);
		hashMap.put("uName", name);
		hashMap.put("uEmail", email);
		hashMap.put("uDp", dp);
		hashMap.put("pTitle", title);
		hashMap.put("pDescr", description);
		hashMap.put("pImage", "noImage");
		hashMap.put("pMode", modeList.get(positionMode));

		DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Post");
		ref.child(editPostId).updateChildren(hashMap)
				.addOnSuccessListener(new OnSuccessListener<Void>() {
					@Override
					public void onSuccess(Void aVoid) {
						progressDialog.dismiss();
						Toast.makeText(AddPostActivity.this, "Updated", Toast.LENGTH_LONG).show();
						finish();
					}
				})
				.addOnFailureListener(new OnFailureListener() {
					@Override
					public void onFailure(@NonNull Exception e) {
						progressDialog.dismiss();
						Toast.makeText(AddPostActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
					}
				});
	}

	private void updateWithImage(final String title, final String description, final String editPostId) {
		// cập nhật lại danh sách ảnh
		// B1: xóa ảnh cũ
		// B2: Tải ảnh mới lên
		final String timestamp = String.valueOf(System.currentTimeMillis());
			n = 0;
			final HashMap<String, Object> hashMap = new HashMap<>();
			hashMap.put("uid", uid);
			hashMap.put("uName", name);
			hashMap.put("uEmail", email);
			hashMap.put("uDp", dp);
			hashMap.put("pTitle", title);
			hashMap.put("pDescr", description);
			hashMap.put("pMode", modeList.get(positionMode));

			for (Uri uri : uriList) {
					ImageView imageView = new ImageView(AddPostActivity.this);
					Picasso.get().load(uri).into(imageView);

					Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
					byte[] bytes = baos.toByteArray();
					String filepathAndName = "Post/" + "post_" + timestamp + "_" + n;
					n++;
					StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(filepathAndName);
					storageReference.putBytes(bytes)
							.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
								@Override
								public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
									Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
									while (!uriTask.isSuccessful()) ;
									urlImg += uriTask.getResult().toString() + ",";
									hashMap.put("pImage", urlImg);

									DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Post");
									ref.child(editPostId).updateChildren(hashMap)
											.addOnSuccessListener(new OnSuccessListener<Void>() {
												@Override
												public void onSuccess(Void aVoid) {
													progressDialog.dismiss();
													Toast.makeText(AddPostActivity.this, "Post publised", Toast.LENGTH_LONG).show();
													uriList.clear();
													imageUri = null;
													finish();
												}
											})
											.addOnFailureListener(new OnFailureListener() {
												@Override
												public void onFailure(@NonNull Exception e) {
													progressDialog.dismiss();
													Toast.makeText(AddPostActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
												}
											});
								}
							})
							.addOnFailureListener(new OnFailureListener() {
								@Override
								public void onFailure(@NonNull Exception e) {
									progressDialog.dismiss();
									Toast.makeText(AddPostActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
								}
							});
			}
			if(uid.equals(hostUid)) {
				for (String item : arrCurrentImg) {
					StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl(item);
					ref.delete();
				}
			}
	}
	private void updateWithNowImage(final String title, final String description, final String editPostId) {
		final String timestamp = String.valueOf(System.currentTimeMillis());
			n = 0;
			final HashMap<String, Object> hashMap = new HashMap<>();
			hashMap.put("uid", uid);
			hashMap.put("uName", name);
			hashMap.put("uEmail", email);
			hashMap.put("uDp", dp);
			hashMap.put("pTitle", title);
			hashMap.put("pDescr", description);
			hashMap.put("pMode", modeList.get(positionMode));

			for (Uri uri : uriList) {
				String filepathAndName = "Post/" + "post_" + timestamp + "_" + n;
				n++;
				StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(filepathAndName);
				storageReference.putFile(uri)
						.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
							@Override
							public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
								Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
								while (!uriTask.isSuccessful()) ;
								urlImg += uriTask.getResult().toString() + ",";
								hashMap.put("pImage", urlImg);

								DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Post");
								ref.child(editPostId).updateChildren(hashMap)
										.addOnSuccessListener(new OnSuccessListener<Void>() {
											@Override
											public void onSuccess(Void aVoid) {
												progressDialog.dismiss();
												Toast.makeText(AddPostActivity.this, "Post publised", Toast.LENGTH_LONG).show();
												uriList.clear();
												imageUri = null;
												finish();
											}
										})
										.addOnFailureListener(new OnFailureListener() {
											@Override
											public void onFailure(@NonNull Exception e) {
												progressDialog.dismiss();
												Toast.makeText(AddPostActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
											}
										});
							}
						})
						.addOnFailureListener(new OnFailureListener() {
							@Override
							public void onFailure(@NonNull Exception e) {
								progressDialog.dismiss();
								Toast.makeText(AddPostActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
							}
						});
			}
	}

	private void uploadData(final String title, final String description) {
		progressDialog.setMessage("Publishing Post");
		progressDialog.show();

		final String timestamp = String.valueOf(System.currentTimeMillis());
		if(!uriList.isEmpty()) {

			n = 0;
			final HashMap<String, Object> hashMap = new HashMap<>();
			hashMap.put("uid", uid);
			hashMap.put("hostUid", uid);
			hashMap.put("uName", name);
			hashMap.put("uEmail", email);
			hashMap.put("pLikes", "0");
			hashMap.put("pComments", "0");
			hashMap.put("uDp", dp);
			hashMap.put("pId", timestamp);
			hashMap.put("pTitle", title);
			hashMap.put("pDescr", description);
			hashMap.put("pTime", timestamp);
			hashMap.put("pMode", modeList.get(positionMode));

			for (Uri uri : uriList) {
				String filepathAndName = "Post/" + "post_" + timestamp + "_" + n;
				n++;
				StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(filepathAndName);
				storageReference.putFile(uri)
						.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
							@Override
							public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
								Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
								while (!uriTask.isSuccessful()) ;
								urlImg += uriTask.getResult().toString() + ",";
								hashMap.put("pImage", urlImg);
								DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Post");
								ref.child(timestamp).setValue(hashMap)
										.addOnSuccessListener(new OnSuccessListener<Void>() {
											@Override
											public void onSuccess(Void aVoid) {
												progressDialog.dismiss();
												uriList.clear();
												imageUri = null;
												finish();
											}
										})
										.addOnFailureListener(new OnFailureListener() {
											@Override
											public void onFailure(@NonNull Exception e) {
												progressDialog.dismiss();
												Toast.makeText(AddPostActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
											}
										});
							}
						})
						.addOnFailureListener(new OnFailureListener() {
							@Override
							public void onFailure(@NonNull Exception e) {
								progressDialog.dismiss();
								Toast.makeText(AddPostActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
							}
						});
			}

			if (n == uriList.size() - 1)
				Toast.makeText(AddPostActivity.this, "Post publised", Toast.LENGTH_LONG).show();
		}
		else
		{
			HashMap<String, Object> hashMap = new HashMap<>();
			hashMap.put("uid", uid);
			hashMap.put("hostUid", uid);
			hashMap.put("uName", name);
			hashMap.put("uEmail", email);
			hashMap.put("uDp", dp);
			hashMap.put("pLikes", "0");
			hashMap.put("pComments", "0");
			hashMap.put("pId", timestamp);
			hashMap.put("pTitle", title);
			hashMap.put("pDescr", description);
			hashMap.put("pImage", "noImage");
			hashMap.put("pTime", timestamp);
			hashMap.put("pMode", modeList.get(positionMode));

			DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Post");
			ref.child(timestamp).setValue(hashMap)
					.addOnSuccessListener(new OnSuccessListener<Void>() {
						@Override
						public void onSuccess(Void aVoid) {
							progressDialog.dismiss();
							Toast.makeText(AddPostActivity.this, "Post publised", Toast.LENGTH_LONG).show();
							txt_inputtitle.setText("");
							txt_description.setText("");
							uriList.clear();
							imageUri = null;
							finish();
						}
					})
					.addOnFailureListener(new OnFailureListener() {
						@Override
						public void onFailure(@NonNull Exception e) {
							progressDialog.dismiss();
							Toast.makeText(AddPostActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
						}
					});
		}
		if (positionMode == 0 ) {
			final String myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
			DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
			reference.addListenerForSingleValueEvent(new ValueEventListener() {
				@Override
				public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
					for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
						if (snapshot.getValue(User.class).getSubscribers().contains(myUid)) {
							sendPostNotification(timestamp, snapshot.getValue(User.class).getUid(), name + " added new post", title);
						}
					}
				}

				@Override
				public void onCancelled(@NonNull DatabaseError databaseError) {

				}
			});
		}

	}

	private void sendPostNotification(final String timestamp, final String uid, final String s, final String body) {
		DatabaseReference allTokens = FirebaseDatabase.getInstance().getReference("Tokens");
		Query query = allTokens.orderByKey().equalTo(uid);
		query.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				for(DataSnapshot snapshot : dataSnapshot.getChildren())
				{
					Token token = snapshot.getValue(Token.class);
					Data data = new Data(timestamp, body,s, uid, "newpost", R.drawable.ic_defaut_img);
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


	private void addControl() {
		actionBar = getSupportActionBar();
		actionBar.setTitle("Add Post");
		actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#2d3447")));
		actionBar.setDisplayShowHomeEnabled(true);
		actionBar.setDisplayShowHomeEnabled(true);

		txt_inputtitle = findViewById(R.id.txt_inputtitle);
		txt_description = findViewById(R.id.txt_description);
		//img_post = findViewById(R.id.img_post);
		btn_upload = findViewById(R.id.btn_upload);
		txt_add_img = findViewById(R.id.txt_add_img);
		frame_img_post = findViewById(R.id.frame_img_post);
		progressDialog = new ProgressDialog(this);
		progressDialog.setCanceledOnTouchOutside(false);
		uriList = new ArrayList<>();
		modeList = new ArrayList<>();
		modeList.add("Publish");
		modeList.add("Private");
		recycler_img_add_post = findViewById(R.id.recycler_img_add_post);
		LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
		recycler_img_add_post.setHasFixedSize(true);
		recycler_img_add_post.setLayoutManager(linearLayoutManager);
		sp_mode = findViewById(R.id.sp_mode);
		modeAdapter = new ArrayAdapter<>(AddPostActivity.this, R.layout.spiner_layout, modeList);
		modeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		sp_mode.setAdapter(modeAdapter);
		requestQueue = Volley.newRequestQueue(getApplicationContext());

		cameraPermission = new String[] {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
		storagePermisstion = new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE};

		firebaseAuth = FirebaseAuth.getInstance();
		checkUserStatus();

		Intent intent = getIntent();
		updateKey = intent.getStringExtra("key");
		editPostId = intent.getStringExtra("editPostId");

		if(updateKey.equals("editPost"))
		{
			actionBar.setTitle("Edit Post");
			btn_upload.setText("Update");
			loadPostData(editPostId);
		}
		else
		{
			actionBar.setTitle("Add Post");
			btn_upload.setText("Upload");
		}
		actionBar.setSubtitle(email);

		reference = FirebaseDatabase.getInstance().getReference("Users");
		Query query = reference.orderByChild("email").equalTo(email);
		query.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				for(DataSnapshot snapshot : dataSnapshot.getChildren())
				{
					name = snapshot.child("name").getValue() + "";
					email = snapshot.child("email").getValue() + "";
					dp = snapshot.child("image").getValue() + "";
				}
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {

			}
		});

	}

	private void loadPostData(final String editPostId) {
		DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Post");
		Query query = ref.orderByChild("pId").equalTo(editPostId);
		query.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				for(DataSnapshot snapshot : dataSnapshot.getChildren())
				{
					editTitle = snapshot.child("pTitle").getValue() + "";
					editDescr = snapshot.child("pDescr").getValue() + "";
					editImage = snapshot.child("pImage").getValue() + "";
					editMode = snapshot.child("pMode").getValue() + "";
					hostUid = snapshot.child("hostUid").getValue() + "";

					txt_inputtitle.setText(editTitle);
					txt_description.setText(editDescr);
					if (editMode.equals("Private"))
					{
						sp_mode.setSelection(1);
					}
					else
					{
						sp_mode.setSelection(0);
					}

					if(!editImage.equals("noImage"))
					{
						arrCurrentImg = editImage.split(",");
						for (String item : arrCurrentImg)
						{
							uriList.add(Uri.parse(item));
						}
						imgAddPostAdapter = new ImgAddPostAdapter(AddPostActivity.this, uriList);
						recycler_img_add_post.setAdapter(imgAddPostAdapter);
					}
				}
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {

			}
		});
	}

	private void showImagePickDialog() {
		String[] option = {"Camera", "Gallery"};

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setItems(option, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(which == 0)
				{
					// show camera
					if(!checkCameraPermission())
						requestCameraPermission();
					else
						pickFromCamera();
				}

				else if(which == 1)
				{
					//show gallery
					if(!checkStoragePermission())
						requestStoragePermission();
					else
						pickFromGallery();
				}
			}
		});

		AlertDialog alertDialog = builder.create();
		alertDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
		alertDialog.show();
	}

	private void pickFromGallery() {
		Intent intent = new Intent(Intent.ACTION_PICK);
		intent.setType("image/*");
		startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
	}

	private void pickFromCamera() {
		ContentValues contentValues = new ContentValues();
		contentValues.put(MediaStore.Images.Media.TITLE, "Temp pic");
		contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Temp Description");
		imageUri = this.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

		Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
		startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_CODE);
	}

	private boolean checkStoragePermission()
	{
		boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
		return result;
	}

	private void requestStoragePermission()
	{
		ActivityCompat.requestPermissions(this, storagePermisstion, STORAGE_REQUEST_CODE);
	}

	private boolean checkCameraPermission()
	{
		boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);

		boolean result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
		return result && result1;
	}
	private void requestCameraPermission()
	{
		ActivityCompat.requestPermissions(this,cameraPermission, CAMERA_REQUEST_CODE);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

		switch (requestCode)
		{
			case CAMERA_REQUEST_CODE: {
				if (grantResults.length > 0) {
					boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
					boolean writeStogareAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
					if (cameraAccepted && writeStogareAccepted) {
						pickFromCamera();
					}
				}
			}
			break;
			case STORAGE_REQUEST_CODE:
			{
				if(grantResults.length > 0)
				{
					boolean writeStogareAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
					if(writeStogareAccepted)
					{
						pickFromGallery();
					}
				}
			}
			break;
		}

		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(resultCode == RESULT_OK)
		{
			if(requestCode == IMAGE_PICK_GALLERY_CODE)
			{
				uriList.add(data.getData());

				imgAddPostAdapter = new ImgAddPostAdapter(AddPostActivity.this, uriList);
				recycler_img_add_post.setAdapter(imgAddPostAdapter);
			}

			if(requestCode == IMAGE_PICK_CAMERA_CODE)
			{
				uriList.add(imageUri);

				imgAddPostAdapter = new ImgAddPostAdapter(AddPostActivity.this, uriList);
				recycler_img_add_post.setAdapter(imgAddPostAdapter);
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public boolean onSupportNavigateUp() {
		onBackPressed();
		return super.onSupportNavigateUp();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_main, menu);

		menu.findItem(R.id.it_add_post).setVisible(false);
		menu.findItem(R.id.it_search).setVisible(false);
		menu.findItem(R.id.it_setting).setVisible(false);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if(id == R.id.it_logout)
		{
			String timestamp = String.valueOf(System.currentTimeMillis());
			checkOnlineStatus(timestamp);
			firebaseAuth.signOut();
			checkUserStatus();
		}
		return super.onOptionsItemSelected(item);
	}

	private void checkUserStatus()
	{
		FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
		if(firebaseUser != null)
		{
			email = firebaseUser.getEmail();
			uid = firebaseUser.getUid();
		}
		else
		{
			Intent intent = new Intent(AddPostActivity.this, MainActivity.class);
			startActivity(intent);
			finish();
		}
	}

	private void checkOnlineStatus(String status)
	{
		FirebaseUser user = firebaseAuth.getCurrentUser();
		DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid());
		HashMap<String, Object> hashMap = new HashMap<>();
		hashMap.put("onlineStatus", status);
		dbRef.updateChildren(hashMap);
	}

	@Override
	protected void onStart() {
		super.onStart();
		checkUserStatus();
	}

	@Override
	protected void onResume() {
		super.onResume();
		checkUserStatus();
	}
}
