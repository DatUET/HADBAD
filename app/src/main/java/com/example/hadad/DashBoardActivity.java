package com.example.hadad;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.view.MenuItem;
import android.widget.Toast;

import com.example.hadad.Notification.Token;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class DashBoardActivity extends AppCompatActivity {

	FirebaseAuth firebaseAuth;
	ActionBar actionBar;
	String mUid, name;
	private static int first = 0;

	BottomNavigationView navigation;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_dashboard);

		addControl();
		addEvent();
	}

	private void addControl() {
		//Actionbar
		actionBar = getSupportActionBar();
		actionBar.setBackgroundDrawable(getDrawable(R.drawable.appbar));

		navigation = findViewById(R.id.navigation);

		firebaseAuth = FirebaseAuth.getInstance();

		actionBar.setTitle("");
		HomeFragment homeFragment = new HomeFragment();
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		transaction.replace(R.id.content, homeFragment);
		transaction.commit();

		checkUserStatus();


	}

	private void addEvent() {
		navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
			@Override
			public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
				switch (menuItem.getItemId()) {
					case R.id.it_home:
						//chuyển qua fragment home
						actionBar.setTitle("");
						HomeFragment homeFragment = new HomeFragment();
						FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
						transaction.replace(R.id.content, homeFragment);
						transaction.commit();

						return true;

					case R.id.it_profile:
						//chuyển qua fragment profile
						actionBar.setTitle("");
						ProfileFragment profileFragment = new ProfileFragment();
						FragmentTransaction transaction1 = getSupportFragmentManager().beginTransaction();
						transaction1.replace(R.id.content, profileFragment);
						transaction1.commit();
						return true;

					case R.id.it_users:
						//chuyển qua fragment users
						actionBar.setTitle("");
						UsersFragment usersFragment = new UsersFragment();
						FragmentTransaction transaction2 = getSupportFragmentManager().beginTransaction();
						transaction2.replace(R.id.content, usersFragment);
						transaction2.commit();
						return true;

					case R.id.it_chat:
						//chuyển qua fragment chats
						actionBar.setTitle("");
						ChatListFragment chatListFragment = new ChatListFragment();
						FragmentTransaction transaction3 = getSupportFragmentManager().beginTransaction();
						transaction3.replace(R.id.content, chatListFragment);
						transaction3.commit();
						return true;
				}
				return false;
			}
		});
	}

	private void updateToken(String token) {
		DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Tokens");
		Token mToken = new Token(token);
		ref.child(mUid).setValue(mToken);
	}

	private void checkUserStatus() {
		FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
		if (firebaseUser != null && firebaseUser.isEmailVerified()) {
			mUid = firebaseUser.getUid();
			SharedPreferences sharedPreferences = getSharedPreferences("SP_USER", MODE_PRIVATE);
			final Editor editor = sharedPreferences.edit();
			DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(mUid).child("name");
			reference.addListenerForSingleValueEvent(new ValueEventListener() {
				@Override
				public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
					name = dataSnapshot.getValue(String.class);
					editor.putString("Current_USER", mUid);
					editor.apply();
				}

				@Override
				public void onCancelled(@NonNull DatabaseError databaseError) {

				}
			});


			updateToken(FirebaseInstanceId.getInstance().getToken());
		} else {
			Intent intent = new Intent(DashBoardActivity.this, MainActivity.class);
			startActivity(intent);
			finish();
		}
	}

	private void checkOnlineStatus(String status) {
		if (mUid != null) {
			DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Users").child(mUid);
			HashMap<String, Object> hashMap = new HashMap<>();
			hashMap.put("onlineStatus", status);
			dbRef.updateChildren(hashMap);
		}
	}

	@Override
	protected void onStart() {
		if(!isOnline()) {
			SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
					.setTitleText("OOP...!")
					.setContentText("No internet. Please connect to internet to use.");
			sweetAlertDialog.setCanceledOnTouchOutside(false);
			sweetAlertDialog.show();
		}
		else {
			checkUserStatus();
			checkOnlineStatus("online");
			if (first == 0) {
				first++;
				startActivity(new Intent(DashBoardActivity.this, SplashActivity.class));
				finish();
			}
		}
		super.onStart();
	}

	@Override
	protected void onResume() {
		checkUserStatus();
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();

	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		finish();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
		if (firebaseUser != null) {
			String timestamp = String.valueOf(System.currentTimeMillis());
			checkOnlineStatus(timestamp);
		}
	}

	private boolean isOnline() {
		NetworkInfo activeNetworkInfo = ((ConnectivityManager)
				getSystemService(CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
		return activeNetworkInfo != null &&
				activeNetworkInfo.isConnectedOrConnecting();
	}
}
