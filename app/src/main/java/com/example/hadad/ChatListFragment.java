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

import com.example.hadad.Adapter.UserChatAdapter;
import com.example.hadad.Model.Chat;
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
public class ChatListFragment extends Fragment {

    FirebaseAuth firebaseAuth;

    RecyclerView recycler_chat_list;
    List<User> userList;
    UserChatAdapter userChatAdapter;
    List<String> userIDList;
    FrameLayout frame_chatlist;
    ProgressBar progressBar;

    public ChatListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chat_list, container, false);

        addControl(view);

        return view;
    }

    private void addControl(View view) {
        firebaseAuth = FirebaseAuth.getInstance();
        recycler_chat_list = view.findViewById(R.id.recycler_chat_list);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recycler_chat_list.setHasFixedSize(true);
        recycler_chat_list.setLayoutManager(linearLayoutManager);
        frame_chatlist = view.findViewById(R.id.frame_chatlist);
        progressBar = view.findViewById(R.id.prg_load);


        userList = new ArrayList<>();
        userIDList = new ArrayList<>();

        getListUserIdChat();
    }

    private void getListUserIdChat() {
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                userIDList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chat chat = snapshot.getValue(Chat.class);
                    String hisUid = "";
                    if (chat.getSender().equals(firebaseUser.getUid()))
                        hisUid = chat.getReciver();
                    else if (chat.getReciver().equals(firebaseUser.getUid()))
                        hisUid = chat.getSender();
                    if (!userIDList.contains(hisUid)) {
                        userIDList.add(hisUid);
                    }
                }
                getListUserChat();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void getListUserChat() {
        userList.clear();
        userIDList.remove("");
        if (!userIDList.isEmpty()) {
            for (String id : userIDList) {
                Log.d("idchat", id);
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users").child(id);
                reference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        User user = dataSnapshot.getValue(User.class);
                        userList.add(user);
                        userChatAdapter = new UserChatAdapter(getActivity(), userList);
                        recycler_chat_list.setAdapter(userChatAdapter);
                        progressBar.setVisibility(View.GONE);
                        frame_chatlist.setBackgroundColor(Color.parseColor("#ECEAEA"));

                        Log.d("userchat", dataSnapshot.toString());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

        }
    }

        private void checkUserStatus () {
            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
            if (firebaseUser != null) {

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
                if (!TextUtils.isEmpty(s.trim())) {
                    //searchUsers(s);
                } else {
                    //getListUserChat();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                Log.d("empty search", TextUtils.isEmpty(s.trim()) + "");
                if (!TextUtils.isEmpty(s.trim())) {
                    //searchUsers(s);
                } else {
                    //getListUserChat();
                }
                return false;
            }
        });

    }

    private void searchUsers(final String s) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    if (userIDList.contains(user.getUid()) && !userList.contains(user)) {
                        if (user.getName().toLowerCase().contains(s.toLowerCase()) ||
                                user.getEmail().toLowerCase().contains(s.toLowerCase())) {
                            userList.add(user);
                        }
                    }
                }
                userChatAdapter = new UserChatAdapter(getActivity(), userList);
                userChatAdapter.notifyDataSetChanged();
                recycler_chat_list.setAdapter(userChatAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.it_logout) {
            String timestamp = String.valueOf(System.currentTimeMillis());
            checkOnlineStatus(timestamp);
            firebaseAuth.signOut();
            checkUserStatus();
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
