package com.example.hadad;


import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.example.hadad.Adapter.UserAdapter;
import com.example.hadad.Model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class UsersFragment extends Fragment {

	RecyclerView recycler_users;
	UserAdapter userAdapter;
	List<User> userList;
	ProgressBar prg_load;
	FrameLayout frame_users;

	FirebaseAuth firebaseAuth;

	public UsersFragment() {
		// Required empty public constructor
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.fragment_users, container, false);

		firebaseAuth = FirebaseAuth.getInstance();

		prg_load = view.findViewById(R.id.prg_load);
		frame_users = view.findViewById(R.id.frame_users);

		recycler_users = view.findViewById(R.id.recycler_users);
		recycler_users.setHasFixedSize(true);
		recycler_users.setLayoutManager(new GridLayoutManager(getActivity(), 2));

		userList = new ArrayList<>();
		getAllUsser();
		return view;
	}

	private void getAllUsser() {
		final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
		DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
		reference.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				userList.clear();
				Log.d("data", dataSnapshot.toString());
				for(DataSnapshot snapshot : dataSnapshot.getChildren())
				{
					User user = snapshot.getValue(User.class);
					if(!user.getUid().equals(firebaseUser.getUid()))
					{
						userList.add(user);
					}
				}
				userAdapter = new UserAdapter(getActivity(), userList, frame_users);
				recycler_users.setAdapter(userAdapter);

				prg_load.setVisibility(View.GONE);
				frame_users.setBackgroundColor(Color.parseColor("#ECEAEA"));
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {

			}
		});
	}

	private void checkUserStatus()
	{
		FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
		if(firebaseUser != null)
		{

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
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.menu_main, menu);

		menu.findItem(R.id.it_add_post).setVisible(false);

		MenuItem menuItem = menu.findItem(R.id.it_search);
		SearchView searchView = (SearchView) MenuItemCompat.getActionView(menuItem);
		searchView.setBackgroundColor(Color.parseColor("#2d3447"));
		searchView.setMaxWidth(Integer.MAX_VALUE);
		searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
			@Override
			public boolean onQueryTextSubmit(String s) {
				if(!TextUtils.isEmpty(s.trim()))
				{
					searchUsers(s);
				}
				else
				{
					getAllUsser();
				}
				return false;
			}

			@Override
			public boolean onQueryTextChange(String s) {
				Log.d("empty search", TextUtils.isEmpty(s.trim()) + "");
				if(!TextUtils.isEmpty(s.trim()))
				{
					searchUsers(s);
				}
				return false;
			}
		});

	}

	private void searchUsers(final String s) {
		final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
		DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
		reference.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				userList.clear();
				for(DataSnapshot snapshot : dataSnapshot.getChildren())
				{
					User user = snapshot.getValue(User.class);
					if(!user.getUid().equals(firebaseUser.getUid()))
					{
						if(user.getName().toLowerCase().contains(s.toLowerCase()) ||
						user.getEmail().toLowerCase().contains(s.toLowerCase())) {
							userList.add(user);
						}
					}
				}
				userAdapter = new UserAdapter(getActivity(), userList, frame_users);
				userAdapter.notifyDataSetChanged();
				recycler_users.setAdapter(userAdapter);
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {

			}
		});
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		if (id ==  R.id.it_logout) {
			String timestamp = String.valueOf(System.currentTimeMillis());
			checkOnlineStatus(timestamp);
			firebaseAuth.signOut();
			checkUserStatus();
		}
		else if (id == R.id.it_setting)
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
