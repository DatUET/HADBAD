package com.example.hadad;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.widget.CompoundButton;

import com.example.hadad.Adapter.UserListPostNotiAdapter;
import com.example.hadad.Model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SettingActivity extends AppCompatActivity {

	SwitchCompat sw_post;
	ActionBar actionBar;
	RecyclerView recycler_user_post;
	List<User> userList;
	UserListPostNotiAdapter userListPostNotiAdapter;

	SharedPreferences preferences;
	SharedPreferences.Editor editor;
	private static final String TOPIC_POST_NOTI = "POST";

	boolean enable4NewPost;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);

		addControl();
		addEvent();
	}

	private void addControl() {
		actionBar = getSupportActionBar();
		actionBar.setTitle("Setting");
		actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#2d3447")));

		//bật chế độ back
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowHomeEnabled(true);
		sw_post = findViewById(R.id.sw_post);
		recycler_user_post = findViewById(R.id.recycler_user_post);
		preferences = getSharedPreferences("NotiPost", MODE_PRIVATE);
		enable4NewPost = preferences.getBoolean(TOPIC_POST_NOTI, false);
		sw_post.setChecked(enable4NewPost);
		userList = new ArrayList<>();
		LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
		recycler_user_post.setLayoutManager(linearLayoutManager);
		if(enable4NewPost)
		{
			addListUser();
		}
	}

	private void addEvent() {
		sw_post.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				editor = preferences.edit();
				editor.putBoolean(TOPIC_POST_NOTI, isChecked);
				editor.apply();

				if(isChecked)
				{
					addListUser();
				}
				else
				{
					userList.clear();
					userListPostNotiAdapter = new UserListPostNotiAdapter(userList, SettingActivity.this);
					recycler_user_post.setAdapter(userListPostNotiAdapter);
					userListPostNotiAdapter.notifyDataSetChanged();
				}
			}
		});
	}

	private void addListUser() {
		String myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
		final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(myUid).child("subscribers");
		ref.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				String[] listUser = dataSnapshot.getValue().toString().split(",");
				userList.clear();
				for(String item : listUser)
				{
					if (!item.equals("")) {
						DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(item);
						reference.addValueEventListener(new ValueEventListener() {
							@Override
							public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
								userList.add(dataSnapshot.getValue(User.class));
								userListPostNotiAdapter = new UserListPostNotiAdapter(userList, SettingActivity.this);
								recycler_user_post.setAdapter(userListPostNotiAdapter);
								userListPostNotiAdapter.notifyDataSetChanged();
							}

							@Override
							public void onCancelled(@NonNull DatabaseError databaseError) {

							}
						});
					}
					else
					{
						userListPostNotiAdapter = new UserListPostNotiAdapter(userList, SettingActivity.this);
						recycler_user_post.setAdapter(userListPostNotiAdapter);
					}
				}

			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {

			}
		});
	}

<<<<<<< HEAD
	private void addControl() {
		actionBar = getSupportActionBar();
		actionBar.setTitle("Setting");
		actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#2d3447")));

		//bật chế độ back
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowHomeEnabled(true);
		sw_post = findViewById(R.id.sw_post);
		recycler_user_post = findViewById(R.id.recycler_user_post);
		preferences = getSharedPreferences("NotiPost", MODE_PRIVATE);
		enable4NewPost = preferences.getBoolean(TOPIC_POST_NOTI, false);
		sw_post.setChecked(enable4NewPost);
		userList = new ArrayList<>();
		LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
		recycler_user_post.setLayoutManager(linearLayoutManager);
		if(enable4NewPost)
		{
			addListUser();
		}
=======
	@Override
	public boolean onSupportNavigateUp() {
		onBackPressed();
		return super.onSupportNavigateUp();
>>>>>>> 6c70d8ed964feb082872ba35e70c6ffa52556402
	}

	@Override
	public boolean onSupportNavigateUp() {
		onBackPressed();
		return super.onSupportNavigateUp();
	}
}