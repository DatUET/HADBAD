package com.example.hadad;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.hadad.Adapter.UserListPostNotiAdapter;
import com.example.hadad.Model.User;
import com.example.hadad.Notification.Data;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class SettingActivity extends AppCompatActivity {

	SwitchCompat sw_post;
	ActionBar actionBar;
	RecyclerView recycler_user_post;
	List<User> userList;
	UserListPostNotiAdapter userListPostNotiAdapter;
	LinearLayout layout_change_pass;

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
		actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#1A1A1A")));

		//bật chế độ back
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowHomeEnabled(true);
		sw_post = findViewById(R.id.sw_post);
		recycler_user_post = findViewById(R.id.recycler_user_post);
		layout_change_pass = findViewById(R.id.layout_change_pass);
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

		layout_change_pass.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(SettingActivity.this, ChangePassActivity.class);
				startActivity(intent);
			}
		});

	}

	private void addListUser() {
		String myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
		final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Follows").child(myUid);
		ref.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				userList.clear();
				if (dataSnapshot.getChildrenCount() > 0) {
					for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
						String idUserFollow = snapshot.getKey();
						DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(idUserFollow);
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

				}
				else {
					userListPostNotiAdapter = new UserListPostNotiAdapter(userList, SettingActivity.this);
					recycler_user_post.setAdapter(userListPostNotiAdapter);
				}
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {

			}
		});
	}

	@Override
	public boolean onSupportNavigateUp() {
		onBackPressed();
		return super.onSupportNavigateUp();
	}
}
