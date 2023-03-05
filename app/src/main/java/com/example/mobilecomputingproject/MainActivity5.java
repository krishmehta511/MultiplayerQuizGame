package com.example.mobilecomputingproject;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity5 extends AppCompatActivity {
    ListView ldb;
    FirebaseDatabase database;
    DatabaseReference ref;
    ArrayList<Integer> images = new ArrayList<>();
    ArrayList<String> playerNames = new ArrayList<>();
    ArrayList<Integer> points = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main5);

        ldb = findViewById(R.id.ldb_list);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                startActivity(new Intent(MainActivity5.this, MainActivity.class));
                finish();
            }
        };
        getOnBackPressedDispatcher().addCallback(callback);

        //Initializing the database
        database = FirebaseDatabase.getInstance();

        eventHandler();

    }

    private void initAdapter(){
        LeaderBoardListItemAdapter adapter =
                new LeaderBoardListItemAdapter(MainActivity5.this, images, playerNames, points);
        ldb.setAdapter(adapter);
    }

    private void eventHandler(){
        ref = database.getReference("players");
        ref.orderByChild("points").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds: snapshot.getChildren()){
                    Log.d("xxxx", ds.getKey());
                    Log.d("xxxx", ds.getValue().toString());
                    playerNames.add(ds.getKey());
                    if((int)(long)ds.child("avatar").getValue() == 1){
                        images.add(R.drawable.m_avatar);
                    } else {
                        images.add(R.drawable.f_avatar);
                    }
                    points.add(-1*(int)(long)ds.child("points").getValue());
                }
                initAdapter();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}