package com.example.hadad;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.FileUtils;
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

import android.provider.OpenableColumns;
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
import com.hitomi.cmlibrary.CircleMenu;
import com.hitomi.cmlibrary.OnMenuSelectedListener;
import com.hitomi.cmlibrary.OnMenuStatusChangeListener;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * màn hình add post
 */
public class AddPostActivity extends AppCompatActivity {

	private static final int CAMERA_REQUEST_CODE = 100;
	private static final int STORAGE_REQUEST_CODE = 200;
	private static final int IMAGE_PICK_GALLERY_CODE = 300;
	private static final int IMAGE_PICK_CAMERA_CODE = 400;

	private static final String MAX_TIME = "9999999999999";

	String cameraPermission[];
	String storagePermisstion[];

	ActionBar actionBar;
	EditText txt_description;
	//ImageView img_post;
	Button btn_upload;
	SweetAlertDialog sweetAlertDialog;
	TextView txt_add_img;
	LinearLayout frame_img_post;

	FirebaseAuth firebaseAuth;
	DatabaseReference reference;

	String name, email, uid, dp;
	String editDescr, editImage, updateKey = "", editPostId = "", editMode = "", hostUid = "";
	RecyclerView recycler_img_add_post;
	Spinner sp_mode;
	List<String> modeList;
	ArrayAdapter<String> modeAdapter;
	RequestQueue requestQueue;
	CircleMenu circle_menu;

	List<Uri> uriList, uriListOfOldPost, uriListToShowImgs;
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

	private void addControl() {
		actionBar = getSupportActionBar();
		actionBar.setTitle("Add Post");
		actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#1A1A1A")));
		actionBar.setDisplayShowHomeEnabled(true);
		actionBar.setDisplayShowHomeEnabled(true);

		txt_description = findViewById(R.id.txt_description);
		//img_post = findViewById(R.id.img_post);
		btn_upload = findViewById(R.id.btn_upload);
		txt_add_img = findViewById(R.id.txt_add_img);
		frame_img_post = findViewById(R.id.frame_img_post);
		sweetAlertDialog = new SweetAlertDialog(this);
		sweetAlertDialog.setCanceledOnTouchOutside(false);
		uriList = new ArrayList<>();
		uriListOfOldPost = new ArrayList<>();
		uriListToShowImgs = new ArrayList<>();
		modeList = new ArrayList<>();
		modeList.add("Public");
		modeList.add("Private");
		circle_menu = findViewById(R.id.circle_menu);
		circle_menu.setMainMenu(Color.parseColor("#C4C4C4"), R.drawable.ic_add, R.drawable.ic_delete_white)
				.addSubMenu(Color.parseColor("#1A1A1A"), R.drawable.ic_camera)
				.addSubMenu(Color.parseColor("#1A1A1A"), R.drawable.ic_gallery);
		recycler_img_add_post = findViewById(R.id.recycler_img_add_post);
		LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
		recycler_img_add_post.setHasFixedSize(true);
		recycler_img_add_post.setLayoutManager(linearLayoutManager);
		sp_mode = findViewById(R.id.sp_mode);
		modeAdapter = new ArrayAdapter<>(AddPostActivity.this, R.layout.spiner_layout, modeList);
		modeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		sp_mode.setAdapter(modeAdapter);
		requestQueue = Volley.newRequestQueue(getApplicationContext());

		cameraPermission = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
		storagePermisstion = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

		firebaseAuth = FirebaseAuth.getInstance();
		checkUserStatus();

		Intent intent = getIntent();
		updateKey = intent.getStringExtra("key");
		editPostId = intent.getStringExtra("editPostId");

		if (updateKey.equals("editPost")) {
			actionBar.setTitle("Edit Post");
			btn_upload.setText("Update");
			loadPostData(editPostId);
		} else {
			actionBar.setTitle("Add Post");
			btn_upload.setText("Upload");
		}
		actionBar.setSubtitle(email);

		reference = FirebaseDatabase.getInstance().getReference("Users");
		Query query = reference.orderByChild("email").equalTo(email);
		query.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
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

	// xử lý sự kiện
	private void addEvent() {

		txt_add_img.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				circle_menu.openMenu();
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
				String description = txt_description.getText().toString();

				if (TextUtils.isEmpty(description)) {
					txt_description.setError("Description is empty");
					return;
				}
				if (updateKey.equals("editPost")) {
					beginUpdate(description, editPostId);
				} else {
					uploadData(description);
				}
			}
		});
	}

	private void beginUpdate(String description, String editPostId) {
		sweetAlertDialog.setTitleText("Updating Post!");
		sweetAlertDialog.setContentText("Please wait...");
		sweetAlertDialog.show();

		uodatePost(description, editPostId);
	}

	private void uodatePost(String description, final String editPostId) {
		final HashMap<String, Object> hashMap = new HashMap<>();
		hashMap.put("uid", uid);
		hashMap.put("uName", name);
		hashMap.put("uEmail", email);
		hashMap.put("uDp", dp);
		hashMap.put("pDescr", description);
		hashMap.put("pMode", modeList.get(positionMode));
		DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Post");

		if (uriListToShowImgs.isEmpty()) {
			hashMap.put("pImage", "noImage");
			ref.child(editPostId).updateChildren(hashMap)
					.addOnSuccessListener(new OnSuccessListener<Void>() {
						@Override
						public void onSuccess(Void aVoid) {
							sweetAlertDialog.dismiss();
							new SweetAlertDialog(AddPostActivity.this, SweetAlertDialog.SUCCESS_TYPE)
									.setTitleText("SUCCESS!")
									.setContentText("Post updated")
									.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
										@Override
										public void onClick(SweetAlertDialog sweetAlertDialog) {
											sweetAlertDialog.dismiss();
											finish();
										}
									})
									.show();
						}
					})
					.addOnFailureListener(new OnFailureListener() {
						@Override
						public void onFailure(@NonNull Exception e) {
							sweetAlertDialog.dismiss();
							new SweetAlertDialog(AddPostActivity.this, SweetAlertDialog.ERROR_TYPE)
									.setTitleText("SORRY!")
									.setContentText("Error! An error occurred. Please try again later")
									.show();
						}
					});
		} else {
			urlImg = "";
			for (Uri uri : uriListOfOldPost) {
				if (uriListToShowImgs.contains(uri)) {
					urlImg += uri.toString() + ",";
				}
			}
			if (!uriList.isEmpty()) {
				final String timestamp = String.valueOf(System.currentTimeMillis());
				n = 0;
				for (Uri uri : uriList) {
					if (uriListToShowImgs.contains(uri)) {
						int quality = 100;

						try {
							InputStream is=  getContentResolver().openInputStream(uri);
							int byte_size = is.available();
							int file_size=byte_size/1024;
							Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
							ByteArrayOutputStream baos = new ByteArrayOutputStream();
							if (file_size > 700)
								quality = (int) (700 / file_size * 100.0);
							bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
							byte[] data = baos.toByteArray();
							String filepathAndName = "Post/" + "post_" + timestamp + "_" + n;
							n++;
							StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(filepathAndName);
							storageReference.putBytes(data)
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
															sweetAlertDialog.dismiss();
															uriList.clear();
															imageUri = null;
															SweetAlertDialog sweetAlertDialog1 = new SweetAlertDialog(AddPostActivity.this, SweetAlertDialog.SUCCESS_TYPE);
															sweetAlertDialog1.setCanceledOnTouchOutside(false);
															sweetAlertDialog1.setTitleText("SUCCESS!")
																	.setContentText("Post publised")
																	.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
																		@Override
																		public void onClick(SweetAlertDialog sweetAlertDialog) {
																			sweetAlertDialog.dismiss();
																			finish();
																		}
																	}).show();

														}
													})
													.addOnFailureListener(new OnFailureListener() {
														@Override
														public void onFailure(@NonNull Exception e) {
															sweetAlertDialog.dismiss();
															new SweetAlertDialog(AddPostActivity.this, SweetAlertDialog.ERROR_TYPE)
																	.setTitleText("SORRY!")
																	.setContentText("Error! An error occurred. Please try again later")
																	.show();
														}
													});
										}
									})
									.addOnFailureListener(new OnFailureListener() {
										@Override
										public void onFailure(@NonNull Exception e) {
											sweetAlertDialog.dismiss();
											new SweetAlertDialog(AddPostActivity.this, SweetAlertDialog.ERROR_TYPE)
													.setTitleText("SORRY!")
													.setContentText("Error! An error occurred. Please try again later")
													.show();
										}
									});
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}
				}
			} else {
				hashMap.put("pImage", urlImg);
				ref.child(editPostId).updateChildren(hashMap)
						.addOnSuccessListener(new OnSuccessListener<Void>() {
							@Override
							public void onSuccess(Void aVoid) {
								sweetAlertDialog.dismiss();
								new SweetAlertDialog(AddPostActivity.this, SweetAlertDialog.SUCCESS_TYPE)
										.setTitleText("SUCCESS!")
										.setContentText("Post updated")
										.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
											@Override
											public void onClick(SweetAlertDialog sweetAlertDialog) {
												sweetAlertDialog.dismiss();
												finish();
											}
										})
										.show();
							}
						})
						.addOnFailureListener(new OnFailureListener() {
							@Override
							public void onFailure(@NonNull Exception e) {
								sweetAlertDialog.dismiss();
								new SweetAlertDialog(AddPostActivity.this, SweetAlertDialog.ERROR_TYPE)
										.setTitleText("SORRY!")
										.setContentText("Error! An error occurred. Please try again later")
										.show();
							}
						});
			}
		}
	}

	private void uploadData(final String description) {
		sweetAlertDialog.setTitleText("Publishing Post!");
		sweetAlertDialog.setContentText("Please wait");
		sweetAlertDialog.show();

		final String timestamp = String.valueOf(System.currentTimeMillis());
		if (!uriList.isEmpty()) {

			n = 0;
			final HashMap<String, Object> hashMap = new HashMap<>();
			hashMap.put("uid", uid);
			hashMap.put("hostUid", uid);
			hashMap.put("uName", name);
			hashMap.put("uEmail", email);
			hashMap.put("uDp", dp);
			hashMap.put("pId", Long.parseLong(MAX_TIME) - Long.parseLong(timestamp) + "");
			hashMap.put("pDescr", description);
			hashMap.put("pTime", timestamp);
			hashMap.put("pMode", modeList.get(positionMode));

			for (Uri uri : uriListToShowImgs) {
				int quality = 100;
				try {
					InputStream is = getContentResolver().openInputStream(uri);
					int byte_size = is.available();
					double file_size = byte_size/1024.0;
					Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					if (file_size > 700)
						quality = (int) (700.0 / file_size * 100.0);
					bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
					Log.d("quality", quality + "");
					byte[] data = baos.toByteArray();
					String filepathAndName = "Post/" + "post_" + timestamp + "_" + n;
					n++;
					StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(filepathAndName);
					storageReference.putBytes(data)
							.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
								@Override
								public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
									Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
									while (!uriTask.isSuccessful()) ;
									urlImg += uriTask.getResult().toString() + ",";
									hashMap.put("pImage", urlImg);
									DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Post");
									ref.child(Long.parseLong(MAX_TIME) - Long.parseLong(timestamp) + "").setValue(hashMap)
											.addOnSuccessListener(new OnSuccessListener<Void>() {
												@Override
												public void onSuccess(Void aVoid) {
												}
											})
											.addOnFailureListener(new OnFailureListener() {
												@Override
												public void onFailure(@NonNull Exception e) {
													sweetAlertDialog.dismiss();
													new SweetAlertDialog(AddPostActivity.this, SweetAlertDialog.ERROR_TYPE)
															.setTitleText("SORRY!")
															.setContentText("Error! An error occurred. Please try again later")
															.show();
												}
											});
								}
							})
							.addOnFailureListener(new OnFailureListener() {
								@Override
								public void onFailure(@NonNull Exception e) {
									sweetAlertDialog.dismiss();
									new SweetAlertDialog(AddPostActivity.this, SweetAlertDialog.ERROR_TYPE)
											.setTitleText("SORRY!")
											.setContentText("Error! An error occurred. Please try again later")
											.show();
								}
							});

					if (n == uriListToShowImgs.size()) {
						sweetAlertDialog.dismiss();
						uriList.clear();
						imageUri = null;
						new SweetAlertDialog(AddPostActivity.this, SweetAlertDialog.SUCCESS_TYPE)
								.setTitleText("SUCCESS!")
								.setContentText("Post publised")
								.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
									@Override
									public void onClick(SweetAlertDialog sweetAlertDialog) {
										sweetAlertDialog.dismiss();
										finish();
									}
								})
								.show();
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		} else {
			HashMap<String, Object> hashMap = new HashMap<>();
			hashMap.put("uid", uid);
			hashMap.put("hostUid", uid);
			hashMap.put("uName", name);
			hashMap.put("uEmail", email);
			hashMap.put("uDp", dp);
			hashMap.put("pId", Long.parseLong(MAX_TIME) - Long.parseLong(timestamp) + "");
			hashMap.put("pDescr", description);
			hashMap.put("pImage", "noImage");
			hashMap.put("pTime", timestamp);
			hashMap.put("pMode", modeList.get(positionMode));

			DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Post");
			ref.child(Long.parseLong(MAX_TIME) - Long.parseLong(timestamp) + "").setValue(hashMap)
					.addOnSuccessListener(new OnSuccessListener<Void>() {
						@Override
						public void onSuccess(Void aVoid) {
							sweetAlertDialog.dismiss();
							txt_description.setText("");
							uriList.clear();
							imageUri = null;
							new SweetAlertDialog(AddPostActivity.this, SweetAlertDialog.SUCCESS_TYPE)
									.setTitleText("SUCCESS!")
									.setContentText("Post publised")
									.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
										@Override
										public void onClick(SweetAlertDialog sweetAlertDialog) {
											sweetAlertDialog.dismiss();
											finish();
										}
									})
									.show();
						}
					})
					.addOnFailureListener(new OnFailureListener() {
						@Override
						public void onFailure(@NonNull Exception e) {
							sweetAlertDialog.dismiss();
							new SweetAlertDialog(AddPostActivity.this, SweetAlertDialog.ERROR_TYPE)
									.setTitleText("SORRY!")
									.setContentText("Error! An error occurred. Please try again later")
									.show();
						}
					});
		}
		if (positionMode == 0) {
			DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Follows");
			reference.addListenerForSingleValueEvent(new ValueEventListener() {
				@Override
				public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
					for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
						if (snapshot.hasChild(uid)) {
							sendPostNotification(timestamp, snapshot.getKey(), name + " added new post", description);
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
				for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
					Token token = snapshot.getValue(Token.class);
					Data data = new Data(timestamp, body, s, uid, "newpost", R.drawable.user);
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

	private void loadPostData(final String editPostId) {
		uriListToShowImgs.clear();
		DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Post");
		Query query = ref.orderByChild("pId").equalTo(editPostId);
		query.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
					editDescr = snapshot.child("pDescr").getValue() + "";
					editImage = snapshot.child("pImage").getValue() + "";
					editMode = snapshot.child("pMode").getValue() + "";
					hostUid = snapshot.child("hostUid").getValue() + "";

					txt_description.setText(editDescr);
					if (editMode.equals("Private")) {
						sp_mode.setSelection(1);
					} else {
						sp_mode.setSelection(0);
					}

					if (!editImage.equals("noImage")) {
						arrCurrentImg = editImage.split(",");
						for (String item : arrCurrentImg) {
							uriListOfOldPost.add(Uri.parse(item));
						}
						uriListToShowImgs.addAll(uriListOfOldPost);
						imgAddPostAdapter = new ImgAddPostAdapter(AddPostActivity.this, uriListToShowImgs);
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
		circle_menu.setVisibility(View.VISIBLE);
		circle_menu.setOnMenuSelectedListener(new OnMenuSelectedListener() {
			@Override
			public void onMenuSelected(int index) {
				if (index == 0) {
					// show camera
					if (!checkCameraPermission())
						requestCameraPermission();
					else
						pickFromCamera();
				} else if (index == 1) {
					//show gallery
					if (!checkStoragePermission())
						requestStoragePermission();
					else
						pickFromGallery();
				}
			}
		})
				.setOnMenuStatusChangeListener(new OnMenuStatusChangeListener() {
					@Override
					public void onMenuOpened() {

					}

					@Override
					public void onMenuClosed() {
						circle_menu.closeMenu();
						circle_menu.setVisibility(View.GONE);
					}
				});
//		String[] option = {"Camera", "Gallery"};
//
//		AlertDialog.Builder builder = new AlertDialog.Builder(this);
//		builder.setItems(option, new DialogInterface.OnClickListener() {
//			@Override
//			public void onClick(DialogInterface dialog, int which) {
//				if (which == 0) {
//					// show camera
//					if (!checkCameraPermission())
//						requestCameraPermission();
//					else
//						pickFromCamera();
//				} else if (which == 1) {
//					//show gallery
//					if (!checkStoragePermission())
//						requestStoragePermission();
//					else
//						pickFromGallery();
//				}
//			}
//		});
//
//		AlertDialog alertDialog = builder.create();
//		alertDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
//		alertDialog.show();
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

	private boolean checkStoragePermission() {
		boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
		return result;
	}

	private void requestStoragePermission() {
		ActivityCompat.requestPermissions(this, storagePermisstion, STORAGE_REQUEST_CODE);
	}

	private boolean checkCameraPermission() {
		boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);

		boolean result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
		return result && result1;
	}

	private void requestCameraPermission() {
		ActivityCompat.requestPermissions(this, cameraPermission, CAMERA_REQUEST_CODE);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

		switch (requestCode) {
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
			case STORAGE_REQUEST_CODE: {
				if (grantResults.length > 0) {
					boolean writeStogareAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
					if (writeStogareAccepted) {
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
		circle_menu.closeMenu();
		circle_menu.setVisibility(View.GONE);
		if (resultCode == RESULT_OK) {
			if (requestCode == IMAGE_PICK_GALLERY_CODE) {
				uriList.add(data.getData());
				uriListToShowImgs.add(data.getData());

				imgAddPostAdapter = new ImgAddPostAdapter(AddPostActivity.this, uriListToShowImgs);
				recycler_img_add_post.setAdapter(imgAddPostAdapter);

				try {
					InputStream is=  getContentResolver().openInputStream(data.getData());
					int byte_size = is.available();
					int file_size=byte_size/1024;
					Toast.makeText(AddPostActivity.this, "file size " + file_size, Toast.LENGTH_LONG).show();
					Log.d("filesize", file_size + "");
				}
				catch (Exception ex)
				{

				}
			}

			if (requestCode == IMAGE_PICK_CAMERA_CODE) {
				uriList.add(imageUri);
				uriListToShowImgs.add(imageUri);

				imgAddPostAdapter = new ImgAddPostAdapter(AddPostActivity.this, uriListToShowImgs);
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
		if (id == R.id.it_logout) {
			String timestamp = String.valueOf(System.currentTimeMillis());
			checkOnlineStatus(timestamp);
			firebaseAuth.signOut();
			checkUserStatus();
		}
		return super.onOptionsItemSelected(item);
	}

	private void checkUserStatus() {
		FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
		if (firebaseUser != null) {
			email = firebaseUser.getEmail();
			uid = firebaseUser.getUid();
		} else {
			Intent intent = new Intent(AddPostActivity.this, MainActivity.class);
			startActivity(intent);
			finish();
		}
	}

	private void checkOnlineStatus(String status) {
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
