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

import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
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

	private static final int ITEM_LOAD = 5;

	FirebaseAuth firebaseAuth;
	RecyclerView recycler_post;
	List<Post> postList;
	static List<String> postKeyList;
	PostAdapter postAdapter;
	ProgressBar prg_load, prg_loadmore;
	SwipeRefreshLayout srl_post;

	Boolean isScrolling = false;
	int currentItem, totalItem, scrollOutItem, indexLastKey = 0;
	DatabaseReference ref;

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
		prg_loadmore = view.findViewById(R.id.prg_loadmore);
		recycler_post = view.findViewById(R.id.recycler_post);
		postKeyList = new ArrayList<>();
		final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
//		linearLayoutManager.setStackFromEnd(true);
//		linearLayoutManager.setReverseLayout(true);
		recycler_post.setLayoutManager(linearLayoutManager);
		postList = new ArrayList<>();
		ref = FirebaseDatabase.getInstance().getReference("Post");
		srl_post = view.findViewById(R.id.srl_post);

		getListKey();
		loadPost();

		recycler_post.addOnScrollListener(new RecyclerView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
				super.onScrollStateChanged(recyclerView, newState);
				if(newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL)
				{
					isScrolling = true;
				}
			}

			@Override
			public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
				super.onScrolled(recyclerView, dx, dy);

				currentItem = linearLayoutManager.getChildCount();
				totalItem = linearLayoutManager.getItemCount();
				scrollOutItem = linearLayoutManager.findFirstVisibleItemPosition();
				if(isScrolling && (currentItem + scrollOutItem == totalItem - 1))
				{
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
				loadPost();
			}
		});

		return view;
	}

	private void getListKey()
	{
		Query query = ref.orderByChild("pMode").equalTo("Public");
		query.addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				postKeyList.clear();
				for(DataSnapshot snapshot : dataSnapshot.getChildren())
				{
					postKeyList.add(snapshot.getKey());
				}
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
				Log.d("err", databaseError.getMessage());
			}
		});

	}

	private void loadmoreData() {
		prg_loadmore.setVisibility(View.VISIBLE);
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				for(int i = indexLastKey;i < indexLastKey + ITEM_LOAD; i++) {
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
				prg_loadmore.setVisibility(View.GONE);
				indexLastKey += ITEM_LOAD;
			}
		}, 1500);
	}


	private void loadPost() {

		Query query = ref.orderByChild("pMode").equalTo("Public").limitToFirst(ITEM_LOAD);
		query.addListenerForSingleValueEvent(new ValueEventListener() {
				@Override
				public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
					postList.clear();
					for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
						postList.add(snapshot.getValue(Post.class));
					}
					postAdapter = new PostAdapter(getActivity(), postList, "transition");
					recycler_post.setAdapter(postAdapter);
					prg_load.setVisibility(View.GONE);
					postAdapter.notifyDataSetChanged();
				}

				@Override
				public void onCancelled(@NonNull DatabaseError databaseError) {
					Toast.makeText(getActivity(), databaseError.getMessage(), Toast.LENGTH_LONG).show();
				}
			});
		indexLastKey += ITEM_LOAD;
		srl_post.setRefreshing(false);
	}

	private void searchPost(final String query)
	{
		ref.addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				postList.clear();
				for(DataSnapshot snapshot : dataSnapshot.getChildren())
				{
					Post post = snapshot.getValue(Post.class);
					if(post.getpDescr().toLowerCase().contains(query.toLowerCase()) && !post.getpMode().equals("Private"))
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
		searchView.setBackgroundColor(Color.parseColor("#2d3447"));
		searchView.setMaxWidth(Integer.MAX_VALUE);
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
//	public void onResume() {
//		super.onResume();
//		indexLastKey = 0;
//		getListKey();
//		loadPost();
//	}
}