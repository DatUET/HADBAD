package com.example.hadad;


import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hadad.Adapter.PostAdapter;
import com.example.hadad.Model.Post;
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
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.hitomi.cmlibrary.CircleMenu;
import com.hitomi.cmlibrary.OnMenuSelectedListener;
import com.hitomi.cmlibrary.OnMenuStatusChangeListener;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.yarolegovich.lovelydialog.LovelyTextInputDialog;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

import static android.app.Activity.RESULT_OK;
import static com.google.firebase.storage.FirebaseStorage.getInstance;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {

	private static final int CAMERA_REQUEST_CODE = 100;
	private static final int STORAGE_REQUEST_CODE = 200;
	private static final int IMAGE_PICK_GALLERY_CODE = 300;
	private static final int IMAGE_PICK_CAMERA_CODE = 400;

	private static final int ITEM_LOAD = 5;

	String cameraPermission[];
	String storagePermisstion[];

	FirebaseAuth firebaseAuth;
	FirebaseUser firebaseUser;
	FirebaseDatabase firebaseDatabase;
	DatabaseReference reference;
	StorageReference storageReference;

	String storagePath = "User_Profile_Cover_Imgs/";

	ImageView img_avatar, img_cover;
	TextView txt_name, txt_email, txt_phone;
	FloatingActionMenu fab;
	FloatingActionButton fab_avatar, fab_cover, fab_phone, fab_name;
	RecyclerView recycler_post;
	SwipeRefreshLayout srl_post;
	FrameLayout rll_fab;

	CircleMenu circle_menu;

	SweetAlertDialog sweetAlertDialog;
	Uri imageUri;

	String profileOrCoverPhoto;

	List<Post> postList;
	List<String> postKeyList;
	PostAdapter postAdapter;
	String uid, imgavarta, imgcover;

	Boolean isScrolling = false;
	int currentItem, totalItem, scrollOutItem, indexLastKey = 0;

	public ProfileFragment() {
		// Required empty public constructor
	}


	@Override
	public View onCreateView(final LayoutInflater inflater, ViewGroup container,
	                         final Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.fragment_profile, container, false);

		firebaseAuth = FirebaseAuth.getInstance();
		firebaseUser = firebaseAuth.getCurrentUser();
		firebaseDatabase = FirebaseDatabase.getInstance();
		reference = firebaseDatabase.getReference("Users");
		storageReference = getInstance().getReference();

		cameraPermission = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
		storagePermisstion = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

		img_avatar = view.findViewById(R.id.img_avatar);
		img_cover = view.findViewById(R.id.img_cover);
		txt_name = view.findViewById(R.id.txt_name);
		txt_email = view.findViewById(R.id.txt_email);
		txt_phone = view.findViewById(R.id.txt_phone);
		srl_post = view.findViewById(R.id.srl_post);
		rll_fab = view.findViewById(R.id.rll_fab);
		fab = view.findViewById(R.id.fab);
		fab_avatar = view.findViewById(R.id.fab_avatar);
		fab_cover = view.findViewById(R.id.fab_cover);
		fab_name = view.findViewById(R.id.fab_name);
		fab_phone = view.findViewById(R.id.fab_phone);
		circle_menu = view.findViewById(R.id.circle_menu);
		circle_menu.setMainMenu(Color.parseColor("#C4C4C4"), R.drawable.ic_add, R.drawable.ic_delete_white)
				.addSubMenu(Color.parseColor("#40C4FF"), R.drawable.ic_camera)
				.addSubMenu(Color.parseColor("#40C4FF"), R.drawable.ic_gallery);
		final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
		recycler_post = view.findViewById(R.id.recycler_post);
		recycler_post.setLayoutManager(linearLayoutManager);
		postList = new ArrayList<>();
		postKeyList = new ArrayList<>();
		fab.setOnMenuButtonClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (fab.isOpened()) {
					fab.close(true);
					rll_fab.setBackgroundColor(Color.parseColor("#00000000"));
				} else {
					fab.open(true);
					rll_fab.setBackgroundColor(Color.parseColor("#80000000"));
				}
				recycler_post.setOnTouchListener(new View.OnTouchListener() {
					@Override
					public boolean onTouch(View v, MotionEvent event) {
						return fab.isOpened();
					}
				});
			}
		});
		sweetAlertDialog = new SweetAlertDialog(getActivity(), SweetAlertDialog.NORMAL_TYPE);
		sweetAlertDialog.setCanceledOnTouchOutside(false);

		Query query = reference.orderByChild("email").equalTo(firebaseUser.getEmail());

		query.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
					String name = snapshot.child("name").getValue() + "";
					String email = snapshot.child("email").getValue() + "";
					String phone = snapshot.child("phone").getValue() + "";
					imgavarta = snapshot.child("image").getValue() + "";
					imgcover = snapshot.child("cover").getValue() + "";

					txt_name.setText(name);
					txt_email.setText(email);
					txt_phone.setText(phone);

					try {
						Picasso.get().load(imgavarta).into(img_avatar);
					} catch (Exception ex) {
						//Picasso.get().load(R.drawable.ic_defaut_avatar).into(img_avatar);
					}
					try {
						Picasso.get().load(imgcover).into(img_cover);
					} catch (Exception ex) {

					}
				}
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {

			}
		});

		fab_avatar.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				profileOrCoverPhoto = "image";
				showImagePicDialog();
			}
		});

		fab_cover.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				profileOrCoverPhoto = "cover";
				showImagePicDialog();
			}
		});

		fab_phone.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showNamePhoneUpdateDialog("phone");
			}
		});

		fab_name.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showNamePhoneUpdateDialog("name");
			}
		});
		img_avatar.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(), ImagePostActivity.class);
				intent.putExtra("pImage", imgavarta);
				startActivity(intent);
			}
		});
		img_cover.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(), ImagePostActivity.class);
				intent.putExtra("pImage", imgcover);
				startActivity(intent);
			}
		});

		checkUserStatus();
		getListKey();
		loadMyPost();

		recycler_post.addOnScrollListener(new RecyclerView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
				super.onScrollStateChanged(recyclerView, newState);
				if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
					isScrolling = true;
				}
			}

			@Override
			public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
				super.onScrolled(recyclerView, dx, dy);

				currentItem = linearLayoutManager.getChildCount();
				totalItem = linearLayoutManager.getItemCount();
				scrollOutItem = linearLayoutManager.findFirstVisibleItemPosition();
				if (isScrolling && (currentItem + scrollOutItem == totalItem)) {
					isScrolling = false;
					loadmoreData();
				}
			}
		});

		srl_post.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				indexLastKey = 0;
				getListKey();
				loadMyPost();
			}
		});

		return view;
	}

	private void loadmoreData() {
		final DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Post");
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				for (int i = indexLastKey; i < indexLastKey + ITEM_LOAD; i++) {
					if (i < postKeyList.size()) {
						ref.child(postKeyList.get(i)).addListenerForSingleValueEvent(new ValueEventListener() {
							@Override
							public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
								postList.add(dataSnapshot.getValue(Post.class));
								postAdapter.notifyDataSetChanged();
							}

							@Override
							public void onCancelled(@NonNull DatabaseError databaseError) {

							}
						});
					}
				}
				indexLastKey += ITEM_LOAD;
			}
		}, 1500);
	}

	private void getListKey() {
		DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Post");
		Query query = ref.orderByChild("uid").equalTo(uid);
		query.addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				postKeyList.clear();
				for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
					postKeyList.add(snapshot.getKey());
				}
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {

			}
		});
	}

	private void loadMyPost() {
		DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Post");
		Query query = ref.orderByChild("uid").equalTo(uid).limitToFirst(ITEM_LOAD);
		query.addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				postList.clear();
				for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
					Post post = snapshot.getValue(Post.class);
					postList.add(post);
				}
				postAdapter = new PostAdapter(getActivity(), postList, "");
				recycler_post.setAdapter(postAdapter);
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
				Toast.makeText(getActivity(), databaseError.getMessage(), Toast.LENGTH_LONG).show();
			}
		});
		indexLastKey += ITEM_LOAD;
		srl_post.setRefreshing(false);
	}

	private void searchMyPost(final String querySearch) {


		DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Post");
		Query query = ref.orderByChild("uid").equalTo(uid);
		query.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				postList.clear();
				for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
					Post post = snapshot.getValue(Post.class);
					if (post.getpDescr().toLowerCase().contains(querySearch.toLowerCase())) {
						postList.add(post);
					}

					postAdapter = new PostAdapter(getActivity(), postList, "");
					recycler_post.setAdapter(postAdapter);
				}
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
				Toast.makeText(getActivity(), databaseError.getMessage(), Toast.LENGTH_LONG).show();
			}
		});
	}

	private boolean checkStoragePermission() {
		boolean result = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
		return result;
	}

	private void requestStoragePermission() {
		requestPermissions(storagePermisstion, STORAGE_REQUEST_CODE);
	}

	private boolean checkCameraPermission() {
		boolean result = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);

		boolean result1 = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
		return result && result1;
	}

	private void requestCameraPermission() {
		requestPermissions(cameraPermission, CAMERA_REQUEST_CODE);
	}

	private void showNamePhoneUpdateDialog(final String key) {
		new LovelyTextInputDialog(getActivity())
				.setTopColor(Color.parseColor("#40C4FF"))
				.setTitle("Update " + key)
				.setMessage("Please enter your new " + key)
				.setInputFilter("Your new " + key + " is empty", new LovelyTextInputDialog.TextFilter() {
					@Override
					public boolean check(String text) {
						return !TextUtils.isEmpty(text.trim());
					}
				})
				.setConfirmButton("Update", new LovelyTextInputDialog.OnTextInputConfirmListener() {
					@Override
					public void onTextInputConfirmed(final String text) {
						sweetAlertDialog.show();
						HashMap<String, Object> result = new HashMap<>();
						result.put(key, text.trim());
						reference.child(firebaseUser.getUid()).updateChildren(result)
								.addOnSuccessListener(new OnSuccessListener<Void>() {
									@Override
									public void onSuccess(Void aVoid) {
										sweetAlertDialog.dismiss();
										new SweetAlertDialog(getActivity(), SweetAlertDialog.SUCCESS_TYPE).setTitleText("Updated!").show();
									}
								})
								.addOnFailureListener(new OnFailureListener() {
									@Override
									public void onFailure(@NonNull Exception e) {
										sweetAlertDialog.dismiss();
										new SweetAlertDialog(getActivity(), SweetAlertDialog.ERROR_TYPE).setTitleText("Error").setContentText(e.getMessage()).show();
										Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
									}
								});

						if (key.equals("name")) {
							DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Post");
							Query query = ref.orderByChild("uid").equalTo(uid);
							query.addListenerForSingleValueEvent(new ValueEventListener() {
								@Override
								public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
									for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
										String child = snapshot.getKey();
										dataSnapshot.getRef().child(child).child("uName").setValue(text.trim());
									}
								}

								@Override
								public void onCancelled(@NonNull DatabaseError databaseError) {

								}
							});

							DatabaseReference refUpdateCmt = FirebaseDatabase.getInstance().getReference("Comments");
							refUpdateCmt.addListenerForSingleValueEvent(new ValueEventListener() {
								@Override
								public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
									for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
										String child = snapshot.getKey();
										String child1 = dataSnapshot.child(child).getKey();
										Query child2 = FirebaseDatabase.getInstance().getReference("Comments").child(child1).orderByChild("uId").equalTo(uid);
										child2.addListenerForSingleValueEvent(new ValueEventListener() {
											@Override
											public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
												for (DataSnapshot snapshot1 : dataSnapshot.getChildren()) {
													String child = snapshot1.getKey();
													dataSnapshot.getRef().child(child).child("uName").setValue(text.trim());
												}
											}

											@Override
											public void onCancelled(@NonNull DatabaseError databaseError) {

											}
										});
									}
								}

								@Override
								public void onCancelled(@NonNull DatabaseError databaseError) {

								}
							});
						}
					}
				})
				.show();

	}

	private void showImagePicDialog() {
		circle_menu.setVisibility(View.VISIBLE);
		circle_menu.setOnMenuSelectedListener(new OnMenuSelectedListener() {
			@Override
			public void onMenuSelected(int index) {
				switch (index) {
					case 0:
						if (!checkCameraPermission())
							requestCameraPermission();
						else
							pickFromCamera();
						break;
					case 1:
						if (!checkStoragePermission())
							requestStoragePermission();
						else
							pickFromGallery();
						break;
				}
			}
		})
				.setOnMenuStatusChangeListener(new OnMenuStatusChangeListener() {
					@Override
					public void onMenuOpened() {

					}

					@Override
					public void onMenuClosed() {
						circle_menu.setVisibility(View.GONE);
					}
				});
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

		if (requestCode == CAMERA_REQUEST_CODE) {
			if (grantResults.length > 0) {
				boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
				boolean writeStogareAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
				if (cameraAccepted && writeStogareAccepted) {
					pickFromCamera();
				}
			}
		} else if (requestCode == STORAGE_REQUEST_CODE) {
			if (grantResults.length > 0) {
				boolean writeStogareAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
				if (writeStogareAccepted) {
					pickFromGallery();
				}
			}
		}

		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		circle_menu.setVisibility(View.GONE);
		if (resultCode == RESULT_OK) {
			if (requestCode == IMAGE_PICK_GALLERY_CODE) {
				imageUri = data.getData();

				uploadProfileCoverPhoto(imageUri);
			}

			if (requestCode == IMAGE_PICK_CAMERA_CODE) {
				uploadProfileCoverPhoto(imageUri);

			}
			fab.close(true);
			rll_fab.setBackgroundColor(Color.parseColor("#00000000"));
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void uploadProfileCoverPhoto(final Uri uri) {
		//upload ảnh lên storage firebase
		int quality = 100;
		Cursor cursor = getActivity().getContentResolver().query(uri,
				null, null, null, null);
		cursor.moveToFirst();
		double file_size = (int) (cursor.getLong(cursor.getColumnIndex(OpenableColumns.SIZE))) / 1024.0;
		cursor.close();
		Log.d("imagesize", file_size + "");
		sweetAlertDialog.show();
		try {
			Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), uri);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			if (file_size > 500)
				quality = (int) (500 / file_size * 100.0);
			bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
			byte[] data = baos.toByteArray();
			String filePathAndName = storagePath + "" + profileOrCoverPhoto + "_" + firebaseUser.getUid();

			StorageReference storageReference1 = storageReference.child(filePathAndName);
			storageReference1.putBytes(data)
					.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
						@Override
						public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
							Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
							while (!uriTask.isSuccessful()) ;
							final Uri downloadUri = uriTask.getResult();
							if (uriTask.isSuccessful()) {
								HashMap<String, Object> result = new HashMap<>();
								result.put(profileOrCoverPhoto, downloadUri.toString());

								reference.child(firebaseUser.getUid()).updateChildren(result)
										.addOnSuccessListener(new OnSuccessListener<Void>() {
											@Override
											public void onSuccess(Void aVoid) {
												sweetAlertDialog.dismiss();
												new SweetAlertDialog(getActivity(), SweetAlertDialog.SUCCESS_TYPE).setTitleText("Updated!").show();
											}
										})
										.addOnFailureListener(new OnFailureListener() {
											@Override
											public void onFailure(@NonNull Exception e) {
												sweetAlertDialog.dismiss();

												Toast.makeText(getActivity(), "Error Update Image...", Toast.LENGTH_LONG).show();
											}
										});
								if (profileOrCoverPhoto.equals("image")) {
									DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Post");
									Query query = ref.orderByChild("uid").equalTo(uid);
									query.addValueEventListener(new ValueEventListener() {
										@Override
										public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
											for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
												String child = snapshot.getKey();
												dataSnapshot.getRef().child(child).child("uDp").setValue(downloadUri.toString());

											}
										}

										@Override
										public void onCancelled(@NonNull DatabaseError databaseError) {

										}
									});

//						ref.addListenerForSingleValueEvent(new ValueEventListener() {
//							@Override
//							public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//								for(DataSnapshot snapshot : dataSnapshot.getChildren())
//								{
//									String child = snapshot.getKey();
//									if(dataSnapshot.child(child).hasChild("Comments"))
//									{
//										String child1 = dataSnapshot.child(child).getKey();
//										Query child2 = FirebaseDatabase.getInstance().getReference("Comments").child(child1).orderByChild("uid").equalTo(uid);
//										child2.addValueEventListener(new ValueEventListener() {
//											@Override
//											public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//												for(DataSnapshot snapshot1 : dataSnapshot.getChildren())
//												{
//													String child = snapshot1.getKey();
//													dataSnapshot.getRef().child(child).child("uDp").setValue(downloadUri.toString());
//												}
//											}
//
//											@Override
//											public void onCancelled(@NonNull DatabaseError databaseError) {
//
//											}
//										});
//									}
//								}
//							}
//
//							@Override
//							public void onCancelled(@NonNull DatabaseError databaseError) {
//
//							}
//						});
									DatabaseReference refUpdateCmt = FirebaseDatabase.getInstance().getReference("Comments");
									refUpdateCmt.addListenerForSingleValueEvent(new ValueEventListener() {
										@Override
										public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
											for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
												String child = snapshot.getKey();
												String child1 = dataSnapshot.child(child).getKey();
												Query child2 = FirebaseDatabase.getInstance().getReference("Comments").child(child1).orderByChild("uId").equalTo(uid);
												child2.addListenerForSingleValueEvent(new ValueEventListener() {
													@Override
													public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
														for (DataSnapshot snapshot1 : dataSnapshot.getChildren()) {
															String child = snapshot1.getKey();
															dataSnapshot.getRef().child(child).child("uDp").setValue(downloadUri.toString());
														}
													}

													@Override
													public void onCancelled(@NonNull DatabaseError databaseError) {

													}
												});
											}
										}

										@Override
										public void onCancelled(@NonNull DatabaseError databaseError) {

										}
									});
								}
							}

						}
					})
					.addOnFailureListener(new OnFailureListener() {
						@Override
						public void onFailure(@NonNull Exception e) {
							sweetAlertDialog.dismiss();
							new SweetAlertDialog(getActivity(), SweetAlertDialog.ERROR_TYPE).setTitleText("Error").setContentText(e.getMessage()).show();
						}
					});
		}
		catch (Exception ex){
			ex.printStackTrace();
		}
	}

	private void pickFromCamera() {
		// chuyển màn hình sang chọn ảnh từ camera
		ContentValues contentValues = new ContentValues();
		contentValues.put(MediaStore.Images.Media.TITLE, "Temp pic");
		contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Temp Description");
		imageUri = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

		Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
		startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_CODE);
	}

	private void pickFromGallery() {
		//chọn từ thư viện
		Intent intent = new Intent(Intent.ACTION_PICK);
		intent.setType("image/*");
		startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
	}

	private void checkUserStatus() {
		FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
		if (firebaseUser != null) {
			uid = firebaseUser.getUid();
		} else {
			Intent intent = new Intent(getActivity(), MainActivity.class);
			startActivity(intent);
			getActivity().finish();
		}
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.menu_main, menu);

		MenuItem item = menu.findItem(R.id.it_search);
		SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
		searchView.setBackgroundColor(Color.parseColor("#2d3447"));
		searchView.setMaxWidth(Integer.MAX_VALUE);
		searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
			@Override
			public boolean onQueryTextSubmit(String s) {
				if (!TextUtils.isEmpty(s)) {
					searchMyPost(s);
				} else {
					loadMyPost();
				}
				return false;
			}

			@Override
			public boolean onQueryTextChange(String s) {
				if (!TextUtils.isEmpty(s)) {
					searchMyPost(s);
				}
				return false;
			}
		});
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.it_logout) {
			String timestamp = String.valueOf(System.currentTimeMillis());
			checkOnlineStatus(timestamp);
			firebaseAuth.signOut();
			checkUserStatus();
		} else if (id == R.id.it_add_post) {
			Intent intent = new Intent(getActivity(), AddPostActivity.class);
			intent.putExtra("key", "addPost");
			startActivity(intent);
		} else if (id == R.id.it_setting) {
			Intent intent = new Intent(getActivity(), SettingActivity.class);
			startActivity(intent);
		}
		return super.onOptionsItemSelected(item);
	}

	private void checkOnlineStatus(String status) {
		FirebaseUser user = firebaseAuth.getCurrentUser();
		DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid());
		HashMap<String, Object> hashMap = new HashMap<>();
		hashMap.put("onlineStatus", status);
		dbRef.updateChildren(hashMap);
	}
}
