package com.example.hadad;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hadad.Adapter.PostAdapter;
import com.example.hadad.Model.Post;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ThereProfileActivity extends AppCompatActivity {

	private static final int REQUEST_CALL = 200;

	ImageView img_avatar, img_cover;
	TextView txt_name, txt_email, txt_phone;
	ActionBar actionBar;

	RecyclerView recycler_post;
	List<Post> postList;
	PostAdapter postAdapter;
	String uid, imgavarta, imgcover;
	LinearLayout layout_profile;
	ImageButton btn_call_phone, btn_chat, btn_subscribe;

	FirebaseAuth firebaseAuth;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_there_profile);

		addControl();
		addEvent();
	}

	private void addControl() {

		recycler_post = findViewById(R.id.recycler_post);
		recycler_post.setNestedScrollingEnabled(false);
		firebaseAuth = FirebaseAuth.getInstance();

		actionBar = getSupportActionBar();
		actionBar.setBackgroundDrawable(getDrawable(R.drawable.appbar));
		actionBar.setTitle("");
		actionBar.setDisplayShowHomeEnabled(true);
		actionBar.setDisplayShowHomeEnabled(true);

		img_avatar = findViewById(R.id.img_avatar);
		img_cover = findViewById(R.id.img_cover);
		txt_name = findViewById(R.id.txt_name);
		txt_email = findViewById(R.id.txt_email);
		txt_phone = findViewById(R.id.txt_phone);
		layout_profile = findViewById(R.id.layout_profile);
		btn_chat = findViewById(R.id.btn_chat);
		btn_call_phone = findViewById(R.id.btn_call_phone);
		btn_subscribe = findViewById(R.id.btn_subscribe);

		Intent intent = getIntent();
		uid = intent.getStringExtra("uid");
		boolean fromUsers = intent.getBooleanExtra("fromUsers", false);
		if(fromUsers)
		{
			img_avatar.setTransitionName("transitionUsers");
		}
		else
		{
			img_avatar.setTransitionName("transition");
		}
		postList = new ArrayList<>();

		Query query = FirebaseDatabase.getInstance().getReference("Users").orderByChild("uid").equalTo(uid);

		query.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				Log.d("user profile", dataSnapshot.toString());
				for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
					String name = snapshot.child("name").getValue() + "";
					String email = snapshot.child("email").getValue() + "";
					String phone = snapshot.child("phone").getValue() + "";
					imgavarta = snapshot.child("image").getValue() + "";
					imgcover = snapshot.child("cover").getValue() + "";


					txt_name.setText(name);
					txt_email.setText(email);
					txt_phone.setText(phone);

					Log.d("user profile", name + " " + email);

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

		checkUserStatus();
		loadHisPost();
	}

	private void addEvent() {
		if (uid.equals(firebaseAuth.getCurrentUser().getUid()))
		{
			btn_chat.setVisibility(View.GONE);
			btn_call_phone.setVisibility(View.GONE);
		}
		img_avatar.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(ThereProfileActivity.this, ImagePostActivity.class);
				intent.putExtra("pImage", imgavarta);
				startActivity(intent);
			}
		});
		img_cover.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(ThereProfileActivity.this, ImagePostActivity.class);
				intent.putExtra("pImage", imgcover);
				startActivity(intent);
			}
		});

		layout_profile.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// đéo làm cl gì cả. Hihi
			}
		});

		btn_chat.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(ThereProfileActivity.this, ChatActivity.class);
				intent.putExtra("uid", uid);
				startActivity(intent);
			}
		});

		btn_call_phone.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				callPhone();
			}
		});

		btn_subscribe.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showDialogAddSubcribe();
			}
		});
	}

	private void showDialogAddSubcribe() {
		FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
		DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid()).child("subscribers");
		ref.addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				String listUser = dataSnapshot.getValue() + "";
				if(listUser.contains(uid))
				{
					AlertDialog.Builder builder = new AlertDialog.Builder(ThereProfileActivity.this);
					builder.setTitle("Subcribe");
					builder.setMessage("Do you unsubscribe " + txt_name.getText().toString());
					builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							unSubscribe();
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
				else
				{
					AlertDialog.Builder builder = new AlertDialog.Builder(ThereProfileActivity.this);
					builder.setTitle("Subcribe");
					builder.setMessage("Do you subscribe " + txt_name.getText().toString());
					builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							addSubscribe();
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
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {

			}
		});


	}

	private void unSubscribe() {
		String myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
		final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(myUid).child("subscribers");
		ref.addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				List<String> listSubscribe = new ArrayList<>();
				listSubscribe.addAll(Arrays.asList(dataSnapshot.getValue().toString().split(",")));
				listSubscribe.remove(uid);
				String afterUnsubcribe = "";
				for(String item : listSubscribe)
				{
					afterUnsubcribe = item +",";
				}
				HashMap<String, Object> hashMap = new HashMap<>();
				hashMap.put("subscribers", afterUnsubcribe);
				ref.getParent().updateChildren(hashMap)
				.addOnCompleteListener(new OnCompleteListener<Void>() {
					                       @Override
					                       public void onComplete(@NonNull Task<Void> task) {
						                       btn_subscribe.setImageResource(R.drawable.ic_subscribe_post);
					                       }
				                       }
				);
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
			}
		});
	}

	private void addSubscribe() {
		String myUid = firebaseAuth.getCurrentUser().getUid();
		final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(myUid).child("subscribers");
		ref.addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				String listUser = dataSnapshot.getValue() + uid + ",";
				HashMap<String, Object> hashMap = new HashMap<>();
				hashMap.put("subscribers", listUser);
				ref.getParent().updateChildren(hashMap);
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
			}
		});

	}

	private void callPhone() {
		if(txt_phone.getText().toString().trim().length() > 0)
		{
			if(ContextCompat.checkSelfPermission(ThereProfileActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED)
			{
				ActivityCompat.requestPermissions(ThereProfileActivity.this, new String[] {Manifest.permission.CALL_PHONE}, REQUEST_CALL);
			}
			else
			{
				String dial = "tel:" + txt_phone.getText().toString();
				startActivity(new Intent(Intent.ACTION_CALL, Uri.parse(dial)));
			}
		}
		else
		{
			Toast.makeText(ThereProfileActivity.this, "This user has not added a phone number yet", Toast.LENGTH_LONG).show();
		}
	}

	private void loadHisPost() {
		LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ThereProfileActivity.this);
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
					if ((!post.getpMode().equals("Private") && !uid.equals(firebaseAuth.getCurrentUser().getUid())) || uid.equals(firebaseAuth.getCurrentUser().getUid())) {
						postList.add(post);
					}

					postAdapter = new PostAdapter(ThereProfileActivity.this, postList, "");
					recycler_post.setAdapter(postAdapter);
				}
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
				Toast.makeText(ThereProfileActivity.this, databaseError.getMessage(), Toast.LENGTH_LONG).show();
			}
		});
	}

	private void searchHisPost(final String querySearch)
	{
		LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ThereProfileActivity.this);
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

					postAdapter = new PostAdapter(ThereProfileActivity.this, postList, "");
					recycler_post.setAdapter(postAdapter);
				}
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
				Toast.makeText(ThereProfileActivity.this, databaseError.getMessage(), Toast.LENGTH_LONG).show();
			}
		});
	}

	private void checkUserStatus()
	{
		FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
		if(firebaseUser != null)
		{
			if(uid.equals(firebaseUser.getUid()))
			{
				btn_chat.setVisibility(View.GONE);
				btn_call_phone.setVisibility(View.GONE);
				btn_subscribe.setVisibility(View.GONE);
			}
			else
			{
				DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid()).child("subscribers");
				ref.addValueEventListener(new ValueEventListener() {
					@Override
					public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
						String listUser = dataSnapshot.getValue() + "";
						if(listUser.contains(uid))
						{
							btn_subscribe.setImageResource(R.drawable.ic_subscribed_post);
						}
					}

					@Override
					public void onCancelled(@NonNull DatabaseError databaseError) {

					}
				});
			}
		}
		else
		{
			Intent intent = new Intent(ThereProfileActivity.this, MainActivity.class);
			startActivity(intent);
			finish();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_main, menu);
		menu.findItem(R.id.it_add_post).setVisible(false);

		MenuItem item = menu.findItem(R.id.it_search);
		SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
		searchView.setBackgroundColor(Color.parseColor("#2d3447"));
		searchView.setMaxWidth(Integer.MAX_VALUE);
		searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
			@Override
			public boolean onQueryTextSubmit(String s) {
				if(!TextUtils.isEmpty(s))
				{
					searchHisPost(s);
				}
				else
				{
					loadHisPost();
				}
				return false;
			}

			@Override
			public boolean onQueryTextChange(String s) {
				if(!TextUtils.isEmpty(s))
				{
					searchHisPost(s);
				}
				return false;
			}
		});
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		if(requestCode == REQUEST_CALL)
		{
			if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
			{
				callPhone();
			}
			else
			{
				Toast.makeText(ThereProfileActivity.this, "Permission Denied!!!", Toast.LENGTH_LONG).show();
			}
		}
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
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
		else if(id == R.id.it_setting)
		{
			Intent intent = new Intent(ThereProfileActivity.this, SettingActivity.class);
			startActivity(intent);
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onSupportNavigateUp() {
		onBackPressed();
		return super.onSupportNavigateUp();
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
