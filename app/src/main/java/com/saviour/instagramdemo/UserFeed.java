package com.saviour.instagramdemo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.saviour.instagramdemo.Adapter.Feed;
import com.saviour.instagramdemo.Model.FeedModel;

public class UserFeed extends AppCompatActivity {

    private RecyclerView recyclerView;
    private Feed adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_feed);
        recyclerView = findViewById(R.id.recyclerview);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        Intent intent = getIntent();
        String Id = intent.getStringExtra("Id");
        Toast.makeText(UserFeed.this,String.valueOf(Id),Toast.LENGTH_SHORT).show();

        Query query = FirebaseDatabase.getInstance().getReference("Images").equalTo("createdBy",Id);
        FirebaseRecyclerOptions<FeedModel> options = new FirebaseRecyclerOptions.Builder<FeedModel>()
                .setQuery(query,FeedModel.class)
                .build();

        adapter = new Feed(options,Id);
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }
}