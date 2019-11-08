package com.example.hadad;


import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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
import android.widget.Toast;

import com.example.hadad.Adapter.PostAdapter;
import com.example.hadad.Model.Post;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

	final static int ITEM_LOAD_MORE = 20;

	FirebaseAuth firebaseAuth;
	RecyclerView recycler_post;
	List<Post> postList;
	PostAdapter postAdapter;
	ProgressBar prg_load;
	FrameLayout frame_home;
	SwipeRefreshLayout sr_reload;
	Query query;


	boolean isLoading = false, isMaxData = false;
	String lastNode = "", lastKey = "";


	public HomeFragment() {
		// Required empty public constructor
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.fragment_home, container, false);

		firebaseAuth = FirebaseAuth.getInstance();
		prg_load = view.findViewById(R.id.prg_load);
		frame_home = view.findViewById(R.id.frame_home);
		sr_reload = view.findViewById(R.id.sr_reload);
		getLastKeyPost();
		recycler_post = view.findViewById(R.id.recycler_post);
		final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
		linearLayoutManager.setStackFromEnd(true);
		linearLayoutManager.setReverseLayout(true);
		recycler_post.setLayoutManager(linearLayoutManager);
		postList = new ArrayList<>();

		loadPost();

		sr_reload.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				isMaxData = false;
				lastNode = postList.get(postList.size() - 1).getpTime();
				postList.remove(postList.size() - 1);
				postAdapter.notifyDataSetChanged();
				getLastKeyPost();
				loadPost();
			}
		});

		return view;
	}

	private void getLastKeyPost() {
		Query getLastKey = FirebaseDatabase.getInstance().getReference()
				.child("Post")
				.orderByKey()
				.limitToLast(1);
		getLastKey.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
					Post post = snapshot.getValue(Post.class);
					{
							lastKey = post.getpTime();
					}
				}
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {

			}
		});

	}

	private void loadPost() {
		if (!isMaxData) {
			if (TextUtils.isEmpty(lastNode)) {
				query = FirebaseDatabase.getInstance().getReference()
						.child("Post")
						.orderByKey()
						.limitToFirst(ITEM_LOAD_MORE);
			} else {
				query = FirebaseDatabase.getInstance().getReference()
						.child("Post")
						.orderByKey()
						.startAt(lastNode)
						.limitToFirst(ITEM_LOAD_MORE);
			}
			query.addListenerForSingleValueEvent(new ValueEventListener() {
				@Override
				public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
					if(dataSnapshot.hasChildren()) {
						List<Post> morePost = new ArrayList<>();
						for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
							Post post = snapshot.getValue(Post.class);
							morePost.add(post);
						}
						lastNode = morePost.get(morePost.size() - 1).getpTime();
						if (!lastNode.equals(lastKey)) {
							morePost.remove(morePost.size() - 1);
						}
						else
							lastNode = "end";
						for (Post p : morePost)
						{
							if (!p.getpMode().equals("Private"))
							{
								postList.add(p);
							}
						}
						morePost.clear();
						postAdapter = new PostAdapter(getActivity(), postList, "transition");
						recycler_post.setAdapter(postAdapter);
						postAdapter.notifyDataSetChanged();
						isLoading = false;
						prg_load.setVisibility(View.GONE);
						frame_home.setBackgroundColor(Color.parseColor("#ECEAEA"));
					}
					else
					{
						isLoading = false;
						isMaxData = true;
					}
				}

				@Override
				public void onCancelled(@NonNull DatabaseError databaseError) {
					isLoading = false;
					Toast.makeText(getActivity(), databaseError.getMessage(), Toast.LENGTH_LONG).show();
				}
			});
		}

		sr_reload.setRefreshing(false);
	}

	private void searchPost(final String query)
	{
		DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Post");
		reference.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				postList.clear();
				for(DataSnapshot snapshot : dataSnapshot.getChildren())
				{
					Post post = snapshot.getValue(Post.class);
					if(post.getpTitle().toLowerCase().contains(query.toLowerCase()) ||
						post.getpDescr().toLowerCase().contains(query.toLowerCase()))
					{
						postList.add(post);
					}


					postAdapter = new PostAdapter(getActivity(), postList, "transition");
					recycler_post.setAdapter(postAdapter);
				}
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
				Toast.makeText(getActivity(),databaseError.getMessage(), Toast.LENGTH_LONG).show();
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
		inflater.inflate(R.menu.menu_main, menu);

		MenuItem item = menu.findItem(R.id.it_search);
		SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
		searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
			@Override
			public boolean onQueryTextSubmit(String s) {
				if(!TextUtils.isEmpty(s))
				{
					searchPost(s);
				}
				else
				{
					loadPost();
					Toast.makeText(getActivity(), "loadpost textsubmit", Toast.LENGTH_LONG).show();
				}
				return false;
			}

			@Override
			public boolean onQueryTextChange(String s) {
				if(!TextUtils.isEmpty(s))
				{
					searchPost(s);
				}
				return false;
			}
		});

		super.onCreateOptionsMenu(menu, inflater);
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

		else if(id == R.id.it_add_post)
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

//	@Override
//	public void onPause() {
//		super.onPause();
//		postList.clear();
//	}
}
