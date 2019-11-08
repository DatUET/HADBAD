package com.example.hadad;

import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;

import com.example.hadad.Adapter.LikedAdapter;
import com.example.hadad.Model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ListLikedActivity extends AppCompatActivity {

    String pId;
    RecyclerView recycler_liked;
    List<User> userList;
    LikedAdapter likedAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_liked);

        Intent intent = getIntent();
        pId = intent.getStringExtra("pId");

        recycler_liked = findViewById(R.id.recycler_liked);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ListLikedActivity.this);
        recycler_liked.setLayoutManager(linearLayoutManager);
        userList = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Likes").child(pId);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren())
                {
                    userList.clear();
                    String uid = snapshot.getKey();
                    Log.d("uid in 46", uid);
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(uid);
                    ref.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            User user = dataSnapshot.getValue(User.class);
                            userList.add(user);
                            likedAdapter = new LikedAdapter(ListLikedActivity.this, userList);
                            recycler_liked.setAdapter(likedAdapter);
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
