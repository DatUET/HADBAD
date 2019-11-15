package com.example.hadad;


import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
	FloatingActionButton fab;
	RecyclerView recycler_post;

	ProgressDialog progressDialog;
	Uri imageUri;

	String profileOrCoverPhoto;

	List<Post> postList;
	PostAdapter postAdapter;
	String uid, imgavarta, imgcover;


	public ProfileFragment() {
		// Required empty public constructor
	}


	@Override
	public View onCreateView(final LayoutInflater inflater, ViewGroup container,
							 final Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View view =  inflater.inflate(R.layout.fragment_profile, container, false);

		firebaseAuth = FirebaseAuth.getInstance();
		firebaseUser = firebaseAuth.getCurrentUser();
		firebaseDatabase = FirebaseDatabase.getInstance();
		reference = firebaseDatabase.getReference("Users");
		storageReference = getInstance().getReference();

		cameraPermission = new String[] {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
		storagePermisstion = new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE};

		img_avatar = view.findViewById(R.id.img_avatar);
		img_cover = view.findViewById(R.id.img_cover);
		txt_name = view.findViewById(R.id.txt_name);
		txt_email = view.findViewById(R.id.txt_email);
		txt_phone = view.findViewById(R.id.txt_phone);
		fab = view.findViewById(R.id.fab);
		recycler_post = view.findViewById(R.id.recycler_post);
		postList = new ArrayList<>();
		progressDialog = new ProgressDialog(getActivity());
		progressDialog.setCanceledOnTouchOutside(false);

		Query query = reference.orderByChild("email").equalTo(firebaseUser.getEmail());

		query.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
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
					}
					catch (Exception ex)
					{
						//Picasso.get().load(R.drawable.ic_defaut_avatar).into(img_avatar);
					}
					try {
						Picasso.get().load(imgcover).into(img_cover);
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

		fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showEditProfileDialog();
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
		loadMyPost();

		return view;
	}

	private void loadMyPost() {
		LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
		linearLayoutManager.setStackFromEnd(true);
		linearLayoutManager.setReverseLayout(true);
		recycler_post.setLayoutManager(linearLayoutManager);

		DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Post");
		Query query = ref.orderByChild("uid").equalTo(uid);
		query.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				postList.clear();
				for (DataSnapshot snapshot : dataSnapshot.getChildren())
				{
					Post post = snapshot.getValue(Post.class);
					postList.add(post);

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

	private void searchMyPost(final String querySearch) {
		LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
		linearLayoutManager.setStackFromEnd(true);
		linearLayoutManager.setReverseLayout(true);
		recycler_post.setLayoutManager(linearLayoutManager);

		DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Post");
		Query query = ref.orderByChild("uid").equalTo(uid);
		query.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				postList.clear();
				for (DataSnapshot snapshot : dataSnapshot.getChildren())
				{
					Post post = snapshot.getValue(Post.class);
					if(post.getpTitle().toLowerCase().contains(querySearch.toLowerCase()) ||
						post.getpDescr().toLowerCase().contains(querySearch.toLowerCase())) {
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

	private boolean checkStoragePermission()
	{
		boolean result = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
		return result;
	}
	private void requestStoragePermission()
	{
		requestPermissions(storagePermisstion, STORAGE_REQUEST_CODE);
	}

	private boolean checkCameraPermission()
	{
		boolean result = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);

		boolean result1 = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
		return result && result1;
	}
	private void requestCameraPermission()
	{
		requestPermissions(cameraPermission, CAMERA_REQUEST_CODE);
	}

	private void showEditProfileDialog() {
		String options[] = {"Edit Profile Picture", "Edit Cover Photo", "Edit name", "Edit Phone"};
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("Choose Action");
		builder.setItems(options, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which)
				{
					case 0:
						progressDialog.setMessage("Updating Profile Picture");
						profileOrCoverPhoto = "image";
						showImagePicDialog();
						break;
					case 1:
						progressDialog.setMessage("Updating Cover Photo");
						profileOrCoverPhoto = "cover";
						showImagePicDialog();
						break;
					case 2:
						progressDialog.setMessage("Updating Name");
						showNamePhoneUpdateDialog("name");
						break;
					case 3:
						progressDialog.setMessage("Updating Phone");
						showNamePhoneUpdateDialog("phone");
						break;
				}
			}
		});

		AlertDialog alertDialog = builder.create();
		alertDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
		alertDialog.show();
	}

	private void showNamePhoneUpdateDialog(final String key) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("Update" + key);

		LinearLayout linearLayout = new LinearLayout(getActivity());
		linearLayout.setOrientation(LinearLayout.VERTICAL);
		linearLayout.setPadding(10, 10, 10, 10);

		final EditText editText = new EditText(getActivity());
		editText.setHint("Enter " + key);

		linearLayout.addView(editText);
		builder.setView(linearLayout);

		builder.setNegativeButton("Update", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				final String value = editText.getText().toString().trim();
				if(!TextUtils.isEmpty(value))
				{
					progressDialog.show();
					HashMap<String, Object> result = new HashMap<>();
					result.put(key, value);
					reference.child(firebaseUser.getUid()).updateChildren(result)
							.addOnSuccessListener(new OnSuccessListener<Void>() {
								@Override
								public void onSuccess(Void aVoid) {
									progressDialog.dismiss();
									Toast.makeText(getActivity(), "Updated...", Toast.LENGTH_LONG).show();
								}
							})
							.addOnFailureListener(new OnFailureListener() {
								@Override
								public void onFailure(@NonNull Exception e) {
									progressDialog.dismiss();
									Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
								}
							});
				}
				else
				{
					Toast.makeText(getActivity(), "Please enter " + key, Toast.LENGTH_LONG).show();
				}

				if(key.equals("name"))
				{
					DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Post");
					Query query = ref.orderByChild("uid").equalTo(uid);
					query.addValueEventListener(new ValueEventListener() {
						@Override
						public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
							for (DataSnapshot snapshot : dataSnapshot.getChildren())
							{
								String child = snapshot.getKey();
								dataSnapshot.getRef().child(child).child("uName").setValue(value);

							}
						}

						@Override
						public void onCancelled(@NonNull DatabaseError databaseError) {

						}
					});

					ref.addListenerForSingleValueEvent(new ValueEventListener() {
						@Override
						public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
							for(DataSnapshot snapshot : dataSnapshot.getChildren())
							{
								String child = snapshot.getKey();
								if(dataSnapshot.child(child).hasChild("Comments"))
								{
									String child1 = dataSnapshot.child(child).getKey();
									Query child2 = FirebaseDatabase.getInstance().getReference("Post").child(child1).child("Comments").orderByChild("uId").equalTo(uid);
									child2.addValueEventListener(new ValueEventListener() {
										@Override
										public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
											for(DataSnapshot snapshot1 : dataSnapshot.getChildren())
											{
												String child = snapshot1.getKey();
												dataSnapshot.getRef().child(child).child("uName").setValue(value);
											}
										}

										@Override
										public void onCancelled(@NonNull DatabaseError databaseError) {

										}
									});
								}
							}
						}

						@Override
						public void onCancelled(@NonNull DatabaseError databaseError) {

						}
					});
				}
			}
		});
		builder.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				progressDialog.dismiss();
			}
		});
		AlertDialog alertDialog = builder.create();
		alertDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
		alertDialog.show();
	}

	private void showImagePicDialog() {
		String options[] = {"Camera", "Gallery"};
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("Pick Image From");
		builder.setItems(options, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which)
				{
					case 0:
						if(!checkCameraPermission())
							requestCameraPermission();
						else
							pickFromCamera();
						break;
					case 1:
						if(!checkStoragePermission())
							requestStoragePermission();
						else
							pickFromGallery();
						break;
				}
			}
		});

		builder.create().show();
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

		if(requestCode == CAMERA_REQUEST_CODE) {
				if (grantResults.length > 0) {
					boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
					boolean writeStogareAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
					if (cameraAccepted && writeStogareAccepted) {
						pickFromCamera();
					}
				}
			}
			else if(requestCode == STORAGE_REQUEST_CODE)
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

		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(resultCode == RESULT_OK)
		{
			if(requestCode == IMAGE_PICK_GALLERY_CODE)
			{
				imageUri = data.getData();

				uploadProfileCoverPhoto(imageUri);
			}

			if(requestCode == IMAGE_PICK_CAMERA_CODE)
			{
				uploadProfileCoverPhoto(imageUri);

			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void uploadProfileCoverPhoto(final Uri uri) {
		//upload ảnh lên storage firebase
		progressDialog.show();
		String filePathAndName = storagePath + "" + profileOrCoverPhoto + "_" + firebaseUser.getUid();

		StorageReference storageReference1 = storageReference.child(filePathAndName);
		storageReference1.putFile(uri)
		.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
			@Override
			public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
				Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
				while (!uriTask.isSuccessful());
				final Uri downloadUri = uriTask.getResult();
				if (uriTask.isSuccessful())
				{
					HashMap<String, Object> result = new HashMap<>();
					result.put(profileOrCoverPhoto, downloadUri.toString());

					reference.child(firebaseUser.getUid()).updateChildren(result)
							.addOnSuccessListener(new OnSuccessListener<Void>() {
								@Override
								public void onSuccess(Void aVoid) {
									progressDialog.dismiss();
									Toast.makeText(getActivity(), "Image Updated...", Toast.LENGTH_LONG).show();
								}
							})
							.addOnFailureListener(new OnFailureListener() {
								@Override
								public void onFailure(@NonNull Exception e) {
									progressDialog.dismiss();
									Toast.makeText(getActivity(), "Error Update Image...", Toast.LENGTH_LONG).show();
								}
							});
					if(profileOrCoverPhoto.equals("image")) {
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

						ref.addListenerForSingleValueEvent(new ValueEventListener() {
							@Override
							public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
								for(DataSnapshot snapshot : dataSnapshot.getChildren())
								{
									String child = snapshot.getKey();
									if(dataSnapshot.child(child).hasChild("Comments"))
									{
										String child1 = dataSnapshot.child(child).getKey();
										Query child2 = FirebaseDatabase.getInstance().getReference("Post").child(child1).child("Comments").orderByChild("uid").equalTo(uid);
										child2.addValueEventListener(new ValueEventListener() {
											@Override
											public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
												for(DataSnapshot snapshot1 : dataSnapshot.getChildren())
												{
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
				progressDialog.dismiss();
				Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
			}
		});
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

	private void checkUserStatus()
	{
		FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
		if(firebaseUser != null)
		{
			uid = firebaseUser.getUid();
		}
		else
		{
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
				if(!TextUtils.isEmpty(s))
				{
					searchMyPost(s);
				}
				else
				{
					loadMyPost();
				}
				return false;
			}

			@Override
			public boolean onQueryTextChange(String s) {
				if(!TextUtils.isEmpty(s))
				{
					searchMyPost(s);
				}
				else
				{
					loadMyPost();
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
		}

		else if (id == R.id.it_add_post)
		{
			Intent intent = new Intent(getActivity(), AddPostActivity.class);
			intent.putExtra("key", "addPost");
			startActivity(intent);
		}
		else if(id == R.id.it_setting)
		{
			Intent intent = new Intent(getActivity(), SettingActivity.class);
			startActivity(intent);
		}
		return super.onOptionsItemSelected(item);
	}

	private void checkOnlineStatus(String status)
	{
		FirebaseUser user = firebaseAuth.getCurrentUser();
		DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid());
		HashMap<String, Object> hashMap = new HashMap<>();
		hashMap.put("onlineStatus", status);
		dbRef.updateChildren(hashMap);
	}
}
